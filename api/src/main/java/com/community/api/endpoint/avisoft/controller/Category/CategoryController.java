package com.community.api.endpoint.avisoft.controller.Category;

import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;
import com.broadleafcommerce.rest.api.wrapper.CategoriesWrapper;
import com.broadleafcommerce.rest.api.wrapper.CategoryWrapper;
import com.broadleafcommerce.rest.api.wrapper.ProductWrapper;
import com.community.api.entity.CategoryDao;
import com.community.api.entity.CustomProduct;
import com.community.api.services.CategoryService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.core.catalog.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/categoryCustom")
public class CategoryController extends CatalogEndpoint {

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
                return new ResponseEntity<>("catalogService is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if(categoryImpl.getName().isEmpty()){
                return new ResponseEntity<>("CategoryTitle cannot be empty or null", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if(categoryImpl.getDescription() == null || categoryImpl.getDescription().isEmpty()) {
                return new ResponseEntity<>("CategoryDescription cannot be empty or null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Category category = catalogService.saveCategory(categoryImpl);

            CategoryWrapper wrapper = (CategoryWrapper) this.context.getBean(CategoryWrapper.class.getName());
            wrapper.wrapDetails(category, request);
            return ResponseEntity.ok(wrapper);

        } catch (Exception e) {
            return new ResponseEntity<>("Some Exception Occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getAllCategories")
    public ResponseEntity<?> getCategories(HttpServletRequest request, @RequestParam(value = "limit", defaultValue = "20") int limit) {
        try {
            if (catalogService == null) {
                return new ResponseEntity<>("catalogService is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Category> categories = this.catalogService.findAllCategories();

            if (categories.isEmpty()) {
                return new ResponseEntity<>("categories not found", HttpStatus.INTERNAL_SERVER_ERROR);
            } else {

                CategoriesWrapper wrapper = (CategoriesWrapper) this.context.getBean(CategoriesWrapper.class.getName());
                wrapper.wrapDetails(categories, request);

                return ResponseEntity.ok(wrapper);

            }
        } catch (Exception e) {
            return new ResponseEntity<>("Some Exception Occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getCategoryById")
    public ResponseEntity<?> getCategoryById(HttpServletRequest request, @RequestParam("categoryId") Long categoryId, @RequestParam(value = "productLimit", defaultValue = "20") int productLimit, @RequestParam(value = "productOffset", defaultValue = "1") int productOffset, @RequestParam(value = "subcategoryLimit", defaultValue = "20") int subcategoryLimit, @RequestParam(value = "subcategoryOffset", defaultValue = "1") int subcategoryOffset) {
        try {
            if (catalogService == null) {
                return new ResponseEntity<>("catalogService is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (categoryId == null) {

                return new ResponseEntity<>("CategoryId not provided", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            Category category = this.catalogService.findCategoryById(categoryId);

            if (category == null) {
                return new ResponseEntity<>("Category not found", HttpStatus.INTERNAL_SERVER_ERROR);
            } else {

                CategoryWrapper wrapper = (CategoryWrapper) this.context.getBean(CategoryWrapper.class.getName());
                wrapper.wrapDetails(category, request);
                return ResponseEntity.ok(wrapper);

            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
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
                CategoryWrapper wrapper = (CategoryWrapper) this.context.getBean(CategoryWrapper.class.getName());
                wrapper.wrapDetails(category, request);
                return ResponseEntity.ok(wrapper);

            } else {
                return new ResponseEntity<>("category not found", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            return new ResponseEntity<>("Some Exception Occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping(value = "/update/{categoryId}")
    public ResponseEntity<?> updateCategoryById(HttpServletRequest request, @RequestBody CategoryImpl categoryImpl, @PathVariable("categoryId") Long id) {
        try {
            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            Category category = this.catalogService.findCategoryById(id);

            if (category != null) {

                // setting the attributes manually
                categoryImpl.getName();
                if(categoryImpl.getName() != null && !categoryImpl.getName().trim().isEmpty()){ // trim works on nonNull values only.
                    category.setName(category.getName().trim());
                }
                if(categoryImpl.getDescription() != null && !categoryImpl.getDescription().trim().isEmpty()){
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
            return new ResponseEntity<>("Some Exception Occurred: "+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @RequestMapping(value = "/getProductsByCategoryId/{categoryId}", method = RequestMethod.GET)
    public ResponseEntity<?> getProductsFromCatrgoryId(HttpServletRequest request, @PathVariable Long categoryId) {
        try {
            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            if (categoryId == null) {
                throw BroadleafWebServicesException.build(404).addMessage("category Id is not provided in request headers.");
            }

            Category category = this.catalogService.findCategoryById(categoryId);

            if (category == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Error retrieving category as There is no category in DB with this Id");
            }

            List<BigInteger> productIdList = customCategoryService.getAllProductsByCategoryId(categoryId);
            List<ProductWrapper> products = new ArrayList<>();

            for(BigInteger productId:productIdList){
                Product product = catalogService.findProductById(productId.longValue());

                ProductWrapper wrapper = (ProductWrapper) this.context.getBean(ProductWrapper.class.getName());
                wrapper.wrapDetails(product, request);
                products.add(wrapper);
            }

            CategoryDao categoryDao = new CategoryDao();
            categoryDao.setCategoryId(category.getId());
            categoryDao.setCategoryName(category.getName());
            categoryDao.setProducts(products);
            categoryDao.setTotalProducts(Long.valueOf(products.size()));

            return ResponseEntity.status(HttpStatus.OK).body(categoryDao);

        } catch (Exception e) {

            String errorMessage = exceptionHandlingService.handleException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);

        }
    }

}
