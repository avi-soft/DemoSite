package com.community.api.endpoint.avisoft.controller.Category;

import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;
import com.broadleafcommerce.rest.api.wrapper.CategoryWrapper;
import com.community.api.entity.CategoryDao;
import com.community.api.entity.CustomCategoryWrapper;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductWrapper;
import com.community.api.services.CategoryService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping(value = "/categoryCustom")
public class CategoryController extends CatalogEndpoint {

    private static final String CATALOGSERVICENOTINITIALIZED = "Catalog service is not initialized.";

    @Autowired
    private ExceptionHandlingService exceptionHandlingService;

    @Autowired
    private CategoryService customCategoryService;

    @PersistenceContext
    private EntityManager entityManager;


    @PostMapping("/add")
    public ResponseEntity<?> addCategory(HttpServletRequest request, @RequestBody CategoryImpl categoryImpl) {
        try {
            if (catalogService == null) {
                return new ResponseEntity<>(CATALOGSERVICENOTINITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (categoryImpl.getName() == null || categoryImpl.getName().trim().isEmpty()) {
                return new ResponseEntity<>("CategoryTitle cannot be empty or null", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            categoryImpl.setName(categoryImpl.getName().trim());
            if (categoryImpl.getDisplayTemplate() == null || categoryImpl.getDisplayTemplate().trim().isEmpty()) {
                return new ResponseEntity<>("CategoryDisplayTemplate cannot be empty or null", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            categoryImpl.setDisplayTemplate(categoryImpl.getDisplayTemplate().trim());
            categoryImpl.setDescription(categoryImpl.getDescription().trim());

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(new Date());
            Date activeStartDate = dateFormat.parse(formattedDate);

            categoryImpl.setActiveStartDate(activeStartDate);
            if (categoryImpl.getActiveEndDate() != null && !categoryImpl.getActiveEndDate().after(categoryImpl.getActiveStartDate())) {
                return new ResponseEntity<>("ActiveEndDate cannot be before or equal to ActiveStartDate(CurrentDate)", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Category category = catalogService.saveCategory(categoryImpl);

//            categoryWrapper is alternative to show data however it's not used as it is not showing the data i want to show.
//            CustomCategoryWrapper wrapper = (CustomCategoryWrapper) this.context.getBean(CustomCategoryWrapper.class.getName());
            CustomCategoryWrapper wrapper = new CustomCategoryWrapper();
            wrapper.wrapDetails(category, request);
            return ResponseEntity.ok(wrapper);

//            // Construct a JSON response
//            Map<String, Object> response = new HashMap<>();
//            response.put("categoryId", category.getId());
//            response.put("categoryName", category.getName());
//            response.put("archived", ((Status) category).getArchived());
//            response.put("displayTemplate", category.getDisplayTemplate());
//            response.put("description", category.getDescription());
//            response.put("ActiveStartDate", category.getActiveStartDate());
//            response.put("ActiveEndDate", category.getActiveEndDate());
//            response.put("Url-Key", category.getUrlKey());
//
//            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("Some Exception Occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getAllCategories")
    public ResponseEntity<?> getCategories(HttpServletRequest request, @RequestParam(value = "limit", defaultValue = "20") int limit) {
        try {
            if (catalogService == null) {
                return new ResponseEntity<>("catalogService is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Category> categories = this.catalogService.findAllCategories();
            List<CustomCategoryWrapper> activeCategories = new ArrayList<>();

            Iterator<Category> iterator = categories.iterator();
            while (iterator.hasNext()) {
                Category category = iterator.next();
                if ((((Status) category).getArchived() != 'Y' && category.getActiveEndDate() == null) || (((Status) category).getArchived() != 'Y' && category.getActiveEndDate().after(new Date()))) {

                    CustomCategoryWrapper wrapper = new CustomCategoryWrapper();
                    wrapper.wrapDetails(category, request);
                    activeCategories.add(wrapper);
                }
            }

            return new ResponseEntity<>(activeCategories, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Some Exception Occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getProductsByCategoryId/{categoryId}")
    public ResponseEntity<?> getProductsByCategoryId(HttpServletRequest request, @PathVariable Long categoryId) {
        try {
            if (catalogService == null) {
                return new ResponseEntity<>("catalogService is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (categoryId == null) {
                return new ResponseEntity<>("Null CategoryId", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Category category = this.catalogService.findCategoryById(categoryId);

            if (category == null) {
                return new ResponseEntity<>("Category not Found", HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (((Status) category).getArchived() == 'Y') {
                return new ResponseEntity<>("Category is Archived", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<BigInteger> productIdList = customCategoryService.getAllProductsByCategoryId(categoryId);
            List<CustomProductWrapper> products = new ArrayList<>();

            for (BigInteger productId : productIdList) {
                CustomProduct customProduct = entityManager.find(CustomProduct.class, productId.longValue());

                CustomProductWrapper wrapper = new CustomProductWrapper();
                wrapper.wrapDetails(customProduct, request);
                products.add(wrapper);
            }

            CategoryDao categoryDao = new CategoryDao();
            categoryDao.setCategoryId(category.getId());
            categoryDao.setCategoryName(category.getName());
            categoryDao.setProducts(products);
            categoryDao.setTotalProducts(Long.valueOf(products.size()));

            return ResponseEntity.status(HttpStatus.OK).body(categoryDao);

        } catch (Exception e) {
            return new ResponseEntity<>("Some Exception Occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/remove/{categoryId}")
    public ResponseEntity<?> removeCategoryById(HttpServletRequest request, @PathVariable("categoryId") Long id, @RequestParam(value = "productLimit", defaultValue = "20") int productLimit, @RequestParam(value = "productOffset", defaultValue = "1") int productOffset, @RequestParam(value = "subcategoryLimit", defaultValue = "20") int subcategoryLimit, @RequestParam(value = "subcategoryOffset", defaultValue = "1") int subcategoryOffset) {
        try {
            if (catalogService == null) {
                return new ResponseEntity<>("catalogService is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Category category = this.catalogService.findCategoryById(id);

            if (category != null) {

                catalogService.removeCategory(category);

                return new ResponseEntity<>("Category Deleted Successfully", HttpStatus.OK);

            } else {
                return new ResponseEntity<>("category not found", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            return new ResponseEntity<>("Some Exception Occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PatchMapping(value = "/update/{categoryId}")
    public ResponseEntity<?> updateCategoryById(HttpServletRequest request, @RequestBody CategoryImpl categoryImpl, @PathVariable("categoryId") Long id) {
        try {
            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            Category category = this.catalogService.findCategoryById(id);

            if (category != null) {

                // setting the attributes manually
                if (categoryImpl.getName() != null && !categoryImpl.getName().trim().isEmpty()) { // trim works on nonNull values only.
                    category.setName(category.getName().trim());
                }
                if (categoryImpl.getDescription() != null && !categoryImpl.getDescription().trim().isEmpty()) {
                    category.setDescription(category.getDescription().trim());
                }

                // Save the updated category
                category = catalogService.saveCategory(category);

                // Wrap and return the updated category details
                CategoryWrapper wrapper = context.getBean(CategoryWrapper.class);
                wrapper.wrapDetails(category, request);
                return ResponseEntity.ok(wrapper);

            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found.");
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Some Exception Occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
