package com.community.api.endpoint.avisoft.controller.Category;

import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.community.api.dto.CategoryDto;
import com.community.api.dto.CustomCategoryWrapper;
import com.community.api.entity.CustomProduct;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.services.CategoryService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
    private static final String CATEGORYCANNOTBELESSTHANOREQAULZERO = "CategoryId cannot be <= 0";
    private static final String SOMEEXCEPTIONOCCURRED = "Some Exception Occurred";

    private ExceptionHandlingService exceptionHandlingService;
    private CategoryService categoryService;

    @Autowired
    public CategoryController(ExceptionHandlingService exceptionHandlingService,CategoryService categoryService)
    {
        this.exceptionHandlingService = exceptionHandlingService;
        this.categoryService = categoryService;
    }

    @PersistenceContext
    private EntityManager entityManager;

    public CategoryController(CategoryService categoryService, EntityManager entityManager){
        this.entityManager = entityManager;
        this.categoryService = categoryService;

    }
    @PostMapping("/add")
    public ResponseEntity<?> addCategory(HttpServletRequest request, @RequestBody CategoryImpl categoryImpl) {
        try {
            if (catalogService == null) {
                return new ResponseEntity<>(CATALOGSERVICENOTINITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (categoryImpl.getName().trim().isEmpty()) {
                return new ResponseEntity<>("CategoryTitle cannot be empty or null", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            categoryImpl.setName(categoryImpl.getName().trim());
            if (categoryImpl.getDisplayTemplate().trim().isEmpty()) {
                return new ResponseEntity<>("CategoryDisplayTemplate cannot be empty", HttpStatus.INTERNAL_SERVER_ERROR);
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

            CustomCategoryWrapper wrapper = new CustomCategoryWrapper();
            wrapper.wrapDetails(category, request);
            return ResponseEntity.ok(wrapper);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(SOMEEXCEPTIONOCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(SOMEEXCEPTIONOCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getProductsByCategoryId")
    public ResponseEntity<?> getProductsByCategoryId(HttpServletRequest request,@RequestParam(value = "id") String id) throws Exception{
        try {
            if (catalogService == null) {
                return new ResponseEntity<>("catalogService is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Long categoryId = Long.parseLong(id);
            if(categoryId <= 0){
                return new ResponseEntity<>(CATEGORYCANNOTBELESSTHANOREQAULZERO, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Category category = this.catalogService.findCategoryById(categoryId);

            if (category == null) {
                return new ResponseEntity<>("Category not Found", HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (((Status) category).getArchived() == 'Y') {
                return new ResponseEntity<>("Category is Archived", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<BigInteger> productIdList = categoryService.getAllProductsByCategoryId(categoryId);
            List<CustomProductWrapper> products = new ArrayList<>();

            for (BigInteger productId : productIdList) {
                CustomProduct customProduct = entityManager.find(CustomProduct.class, productId.longValue());

                if(customProduct != null && (((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {
                    CustomProductWrapper wrapper = new CustomProductWrapper();
                    wrapper.wrapDetails(customProduct);
                    products.add(wrapper);
                }
            }

            CategoryDto categoryDao = new CategoryDto();
            categoryDao.setCategoryId(category.getId());
            categoryDao.setCategoryName(category.getName());
            categoryDao.setProducts(products);
            categoryDao.setTotalProducts(Long.valueOf(products.size()));

            return ResponseEntity.status(HttpStatus.OK).body(categoryDao);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(SOMEEXCEPTIONOCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/remove/{categoryId}")
    public ResponseEntity<?> removeCategoryById(HttpServletRequest request, @PathVariable("categoryId") String id, @RequestParam(value = "productLimit", defaultValue = "20") int productLimit, @RequestParam(value = "productOffset", defaultValue = "1") int productOffset, @RequestParam(value = "subcategoryLimit", defaultValue = "20") int subcategoryLimit, @RequestParam(value = "subcategoryOffset", defaultValue = "1") int subcategoryOffset) {
        try {
            if (catalogService == null) {
                return new ResponseEntity<>("catalogService is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Long categoryId = Long.parseLong(id);
            if(categoryId <= 0){
                return new ResponseEntity<>(CATEGORYCANNOTBELESSTHANOREQAULZERO, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Category category = this.catalogService.findCategoryById(categoryId);

            if (category != null) {

                catalogService.removeCategory(category);

                return new ResponseEntity<>("Category Deleted Successfully", HttpStatus.OK);

            } else {
                return new ResponseEntity<>("category not found", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(SOMEEXCEPTIONOCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PatchMapping(value = "/update/{categoryId}")
    public ResponseEntity<?> updateCategoryById(HttpServletRequest request, @RequestBody CategoryImpl categoryImpl, @PathVariable("categoryId") String id)
    {
        try {

            Long categoryId = Long.parseLong(id);
            if(categoryId <= 0){
                return new ResponseEntity<>(CATEGORYCANNOTBELESSTHANOREQAULZERO, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (catalogService == null) {
                return new ResponseEntity<>(CATALOGSERVICENOTINITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Category category = this.catalogService.findCategoryById(categoryId);

            if (category != null) {

                // setting the attributes manually
                if (!categoryImpl.getName().isEmpty() && !categoryImpl.getName().trim().isEmpty()) { // trim works on nonNull values only.
                    category.setName(categoryImpl.getName().trim());
                }
                if (categoryImpl.getDescription() != null && !categoryImpl.getDescription().trim().isEmpty()) {
                    category.setDescription(categoryImpl.getDescription().trim());
                }
                if (categoryImpl.getActiveEndDate() != null && !categoryImpl.getActiveEndDate().after(categoryImpl.getActiveStartDate()) && !categoryImpl.getActiveEndDate().after(new Date())) {
                    return new ResponseEntity<>("ActiveEndDate cannot be before or equal to ActiveStartDate(CurrentDate)", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                if (categoryImpl.getDisplayTemplate() != null && !categoryImpl.getDisplayTemplate().trim().isEmpty()) {
                    category.setDisplayTemplate(categoryImpl.getDescription().trim());
                }

                // Save the updated category
                category = catalogService.saveCategory(category);

                // Wrap and return the updated category details
                CustomCategoryWrapper wrapper = new CustomCategoryWrapper();
                wrapper.wrapDetails(category, request);
                return ResponseEntity.ok(wrapper);

            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found.");
            }
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(SOMEEXCEPTIONOCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
