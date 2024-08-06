package com.community.api.endpoint.avisoft.controller.Category;

import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;
import com.broadleafcommerce.rest.api.wrapper.CategoriesWrapper;
import com.broadleafcommerce.rest.api.wrapper.CategoryWrapper;
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

            catalogService.saveCategory(categoryImpl);

            CategoryWrapper wrapper = (CategoryWrapper) this.context.getBean(CategoryWrapper.class.getName());
            wrapper.wrapDetails(categoryImpl, request);
            return ResponseEntity.ok(wrapper);

        } catch (Exception e) {
            return new ResponseEntity<>("Some Exception Occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/categories")
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

    @GetMapping(value = "/category")
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

    @RequestMapping(value = "/update/{categoryId}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateCategoryById(HttpServletRequest request, @PathVariable("categoryId") Long id, @RequestParam(value = "productLimit", defaultValue = "20") int productLimit, @RequestParam(value = "productOffset", defaultValue = "1") int productOffset, @RequestParam(value = "subcategoryLimit", defaultValue = "20") int subcategoryLimit, @RequestParam(value = "subcategoryOffset", defaultValue = "1") int subcategoryOffset) {
        try {
            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            Category category = this.catalogService.findCategoryById(id);

            if (category != null) {

                category.setName("Clothing");
                catalogService.saveCategory(category);
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


    @RequestMapping(value = "/products/{categoryId}", method = RequestMethod.GET)
    public ResponseEntity<?> getProductsFromCatrgoryId(@PathVariable Long categoryId) {
        try {
            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            if (categoryId == null) {
                throw BroadleafWebServicesException.build(404).addMessage("category Id is not provided in request headers.");
            }

            Category category1 = this.catalogService.findCategoryById(categoryId);

            if (category1 == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Error retrieving category as There is no category in DB with this Id");
            }

            List<CategoryProductXref> categoryProductXrefList = customCategoryService.getAllProductsByCategoryId(categoryId);
            Category category = catalogService.findCategoryById(categoryId);
            Long ch = categoryProductXrefList.get(0).getId();

//            CustomCategoryDao customCategoryDao = new CustomCategoryDao();
//            customCategoryDao.setCategoryId(category.getId());
//            customCategoryDao.setCategoryName(category.getMetaTitle());
//            customCategoryDao.setProducts(productList);

            System.out.println(categoryProductXrefList.size());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("customCategoryDao");

        } catch (Exception e) {

            String errorMessage = exceptionHandlingService.handleException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);

        }
    }

}



/*
* package com.community.api.endpoint.avisoft;

import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;
import com.broadleafcommerce.rest.api.wrapper.CategoriesWrapper;
import com.broadleafcommerce.rest.api.wrapper.CategoryAttributeWrapper;
import com.broadleafcommerce.rest.api.wrapper.CategoryWrapper;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryAttribute;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

//, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }

@RestController
@RequestMapping(value = "/category")
public class CustomCategoryEndpoint extends CatalogEndpoint{

    private static final Logger logger = LoggerFactory.getLogger(CustomCategoryEndpoint.class);

    private ExceptionHandlingService exceptionHandlingService;

    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;

    @RequestMapping(value = "/categories", method = RequestMethod.GET)
    public ResponseEntity<?> getCategories(HttpServletRequest request, @RequestParam(value = "limit",defaultValue = "20") int limit) {
        try {
            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            List<Category> categories = this.catalogService.findAllCategories();

            if (categories.size() == 0) {
                throw BroadleafWebServicesException.build(404).addMessage("Error retrieving category as There is no category in DB");
            }else{
                CategoriesWrapper wrapper = (CategoriesWrapper)this.context.getBean(CategoriesWrapper.class.getName());
                wrapper.wrapDetails(categories, request);

                return ResponseEntity.ok(wrapper);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }

    @RequestMapping(value = "", method = RequestMethod.GET, params = {"categoryId"})
    public ResponseEntity<?> getCategoryById(HttpServletRequest request, @RequestParam("categoryId") Long id, @RequestParam(value = "productLimit",defaultValue = "20") int productLimit, @RequestParam(value = "productOffset",defaultValue = "1") int productOffset, @RequestParam(value = "subcategoryLimit",defaultValue = "20") int subcategoryLimit, @RequestParam(value = "subcategoryOffset",defaultValue = "1") int subcategoryOffset) {
        try {
            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            if(id == null){
                throw BroadleafWebServicesException.build(404).addMessage("category Id is not provided in request headers.");
            }
            Category cat = this.catalogService.findCategoryById(id);

            if (cat == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Error retrieving category as There is no category in DB with this Id");
            }else{
                CategoryWrapper wrapper = (CategoryWrapper)this.context.getBean(CategoryWrapper.class.getName());
                wrapper.wrapDetails(cat, request);
                return ResponseEntity.ok(wrapper);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }

    @RequestMapping(value = "/subcategories/{categoryId}", method = RequestMethod.GET)
    public ResponseEntity<?> getSubCategories(HttpServletRequest request, @PathVariable("categoryId") Long id ){

        try {
            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            final Category category = this.catalogService.findCategoryById(id);

            if (category == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Error retrieving subcategory as There is no category in DB with this Id");
            }else{
                CategoriesWrapper wrapper = (CategoriesWrapper)this.context.getBean(CategoriesWrapper.class.getName());
                List<Category> categories = this.catalogService.findAllSubCategories(category);

                wrapper.wrapDetails(categories, request);
                return ResponseEntity.ok(wrapper);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ResponseEntity<?> addCategory(HttpServletRequest request){
        try {
            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            final Category category =  this.catalogService.createCategory();

            //        Long categoryId = new Long(500);
            category.setId(500L);
            category.setName("Grocery");
            category.setUrl("/test-category4");

            catalogService.saveCategory(category);

            CategoryWrapper wrapper = (CategoryWrapper)this.context.getBean(CategoryWrapper.class.getName());
            wrapper.wrapDetails(category,request);
            return ResponseEntity.ok(wrapper);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }

    @RequestMapping(value = "/remove/{categoryId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeCategoryById(HttpServletRequest request, @PathVariable("categoryId") Long id , @RequestParam(value = "productLimit",defaultValue = "20") int productLimit, @RequestParam(value = "productOffset",defaultValue = "1") int productOffset, @RequestParam(value = "subcategoryLimit",defaultValue = "20") int subcategoryLimit, @RequestParam(value = "subcategoryOffset",defaultValue = "1") int subcategoryOffset) {
        try {
            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            final Category category = this.catalogService.findCategoryById(id);

            if (category != null) {
                catalogService.removeCategory(category);
                CategoryWrapper wrapper = (CategoryWrapper)this.context.getBean(CategoryWrapper.class.getName());
                wrapper.wrapDetails(category, request);
                return ResponseEntity.ok(wrapper);
            } else {
                throw BroadleafWebServicesException.build(404).addMessage("Product not available", id);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }

    @RequestMapping(value = "/update/{categoryId}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateCategoryById(HttpServletRequest request, @PathVariable("categoryId") Long id , @RequestParam(value = "productLimit",defaultValue = "20") int productLimit, @RequestParam(value = "productOffset",defaultValue = "1") int productOffset, @RequestParam(value = "subcategoryLimit",defaultValue = "20") int subcategoryLimit, @RequestParam(value = "subcategoryOffset",defaultValue = "1") int subcategoryOffset) {
        try {
            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }
            final Category category = this.catalogService.findCategoryById(id);
            if (category != null) {
                category.setName("Clothing");
                catalogService.saveCategory(category);
                CategoryWrapper wrapper = (CategoryWrapper)this.context.getBean(CategoryWrapper.class.getName());
                wrapper.wrapDetails(category, request);
                return ResponseEntity.ok(wrapper);
            } else {
                throw BroadleafWebServicesException.build(404).addMessage("There is no category with this id to update", id);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }

    @RequestMapping(value = "/attributes/{categoryId}", method = RequestMethod.GET)
    public ResponseEntity<?> getCategoryAttributes(HttpServletRequest request, @PathVariable("categoryId") Long categoryId) {
        try {
            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            Category category = this.catalogService.findCategoryById(categoryId);

            if (category == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found with ID: " + categoryId);
            } else {
                List<CategoryAttributeWrapper> categoryAttributeWrapperList = new ArrayList<>();
                if (category.getCategoryAttributesMap() != null) {
                    for (String key : category.getCategoryAttributesMap().keySet()) {
                        CategoryAttributeWrapper wrapper = (CategoryAttributeWrapper) this.context.getBean(CategoryAttributeWrapper.class.getName());
                        wrapper.wrapSummary((CategoryAttribute) category.getCategoryAttributesMap().get(key), request);
                        categoryAttributeWrapperList.add(wrapper);
                    }
                }
                return ResponseEntity.ok(categoryAttributeWrapperList);
            }
        } catch (Exception e) {
            String errorMessage = exceptionHandlingService.handleException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }
}
*/