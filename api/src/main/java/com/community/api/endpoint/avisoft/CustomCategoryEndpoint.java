package com.community.api.endpoint.avisoft;

import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;
import com.broadleafcommerce.rest.api.wrapper.CategoriesWrapper;
import com.broadleafcommerce.rest.api.wrapper.CategoryAttributeWrapper;
import com.broadleafcommerce.rest.api.wrapper.CategoryWrapper;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryAttribute;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
@RestController
@RequestMapping(value = "/category")
public class CustomCategoryEndpoint extends CatalogEndpoint{

    private static final Logger logger = LoggerFactory.getLogger(CustomCategoryEndpoint.class);

    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;

    @RequestMapping(value = "/categories", method = RequestMethod.GET)
    public CategoriesWrapper getCategories(HttpServletRequest request, @RequestParam(value = "limit",defaultValue = "20") int limit) {
        try {
            if (catalogService == null) {
                logger.error("Catalog service is not initialized.");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }

            List<Category> categories = this.catalogService.findAllCategories();

            if (categories.size() == 0) {
                logger.error("Error retrieving category as There is no category in DB");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.categoryNotFound");
            }else{
                logger.info("TILL HERE");
                CategoriesWrapper wrapper = (CategoriesWrapper)this.context.getBean(CategoriesWrapper.class.getName());
                wrapper.wrapDetails(categories, request);

                return wrapper;
            }
        } catch (RuntimeException e) {
            logger.error("Error retrieving category");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
        }
    }

    @RequestMapping(value = "", method = RequestMethod.GET, params = {"categoryId"})
    public CategoryWrapper getCategoryById(HttpServletRequest request, @RequestParam("categoryId") Long id, @RequestParam(value = "productLimit",defaultValue = "20") int productLimit, @RequestParam(value = "productOffset",defaultValue = "1") int productOffset, @RequestParam(value = "subcategoryLimit",defaultValue = "20") int subcategoryLimit, @RequestParam(value = "subcategoryOffset",defaultValue = "1") int subcategoryOffset) {
        try {
            if (catalogService == null) {
                logger.error("Catalog service is not initialized.");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }

            if(id == null){
                logger.error("category Id is not provided in request headers.");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }
            Category cat = this.catalogService.findCategoryById(id);

            if (cat == null) {
                logger.error("Error retrieving category as There is no category in DB with this Id");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.categoryNotFound");
            }else{
                CategoryWrapper wrapper = (CategoryWrapper)this.context.getBean(CategoryWrapper.class.getName());
                wrapper.wrapDetails(cat, request);
                return wrapper;
            }

        } catch (RuntimeException e) {
            logger.error("Error retrieving category");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
        }
    }

    @RequestMapping(value = "/subcategories/{categoryId}", method = RequestMethod.GET)
    public CategoriesWrapper getSubCategories(HttpServletRequest request, @PathVariable("categoryId") Long id ){

        try {
            if (catalogService == null) {
                logger.error("Catalog service is not initialized.");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }

            final Category category = this.catalogService.findCategoryById(id);

            if (category == null) {
                logger.error("Error retrieving subcategory as There is no category in DB with this Id");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.categoryNotFound");
            }else{
                CategoriesWrapper wrapper = (CategoriesWrapper)this.context.getBean(CategoriesWrapper.class.getName());
                List<Category> categories = this.catalogService.findAllSubCategories(category);

                wrapper.wrapDetails(categories, request);
                return wrapper;
            }

        } catch (RuntimeException e) {
            logger.error("Error retrieving category");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public CategoryWrapper addCategory(HttpServletRequest request){
        try {
            if (catalogService == null) {
                logger.error("Catalog service is not initialized.");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }

            final Category category =  this.catalogService.createCategory();

            //        Long categoryId = new Long(500);
            category.setId(500L);
            category.setName("Grocery");
            category.setUrl("/test-category4");

            catalogService.saveCategory(category);

            CategoryWrapper wrapper = (CategoryWrapper)this.context.getBean(CategoryWrapper.class.getName());
            wrapper.wrapDetails(category,request);
            return wrapper;

        }catch (RuntimeException e) {
            logger.error("Error adding category");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
        }
    }

    @RequestMapping(value = "/remove/{categoryId}", method = RequestMethod.DELETE)
    public CategoryWrapper removeCategoryById(HttpServletRequest request, @PathVariable("categoryId") Long id , @RequestParam(value = "productLimit",defaultValue = "20") int productLimit, @RequestParam(value = "productOffset",defaultValue = "1") int productOffset, @RequestParam(value = "subcategoryLimit",defaultValue = "20") int subcategoryLimit, @RequestParam(value = "subcategoryOffset",defaultValue = "1") int subcategoryOffset) {
        try {
            if (catalogService == null) {
                logger.error("Catalog service is not initialized.");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }

            final Category category = this.catalogService.findCategoryById(id);

            if (category != null) {
                catalogService.removeCategory(category);
                CategoryWrapper wrapper = (CategoryWrapper)this.context.getBean(CategoryWrapper.class.getName());
                wrapper.wrapDetails(category, request);
                return wrapper;
            } else {
                logger.error("There is no category with this id to delete");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.categoryNotFound", id);
            }
        }catch (RuntimeException e) {
            logger.error("Error removing category");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
        }
    }

    @RequestMapping(value = "/update/{categoryId}", method = RequestMethod.PUT)
    public CategoryWrapper updateCategoryById(HttpServletRequest request, @PathVariable("categoryId") Long id , @RequestParam(value = "productLimit",defaultValue = "20") int productLimit, @RequestParam(value = "productOffset",defaultValue = "1") int productOffset, @RequestParam(value = "subcategoryLimit",defaultValue = "20") int subcategoryLimit, @RequestParam(value = "subcategoryOffset",defaultValue = "1") int subcategoryOffset) {
        try {
            if (catalogService == null) {
                logger.error("Catalog service is not initialized.");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }
            final Category category = this.catalogService.findCategoryById(id);
            if (category != null) {
                category.setName("Clothing");
                catalogService.saveCategory(category);
                CategoryWrapper wrapper = (CategoryWrapper)this.context.getBean(CategoryWrapper.class.getName());
                wrapper.wrapDetails(category, request);
                return wrapper;
            } else {
                logger.error("There is no category with this id to update");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.categoryNotFound", id);
            }
        }catch (RuntimeException e) {
            logger.error("Error updating category");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
        }
    }

    @RequestMapping(value = "/attributes/{categoryId}", method = RequestMethod.GET)
    public List<CategoryAttributeWrapper> getCategoryAttributes(HttpServletRequest request, @PathVariable("categoryId") Long categoryId) {
        try {
            if (catalogService == null) {
                logger.error("Catalog service is not initialized.");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }

            Category category = this.catalogService.findCategoryById(categoryId);

            if (category == null) {
                logger.error("There is no category with this id for finding the attributes");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.categoryNotFound", categoryId);
            } else {
                ArrayList<CategoryAttributeWrapper> out = new ArrayList();
                if (category.getCategoryAttributesMap() != null) {
                    Iterator var5 = category.getCategoryAttributesMap().keySet().iterator();

                    while(var5.hasNext()) {
                        String key = (String)var5.next();
                        CategoryAttributeWrapper wrapper = (CategoryAttributeWrapper)this.context.getBean(CategoryAttributeWrapper.class.getName());
                        wrapper.wrapSummary((CategoryAttribute)category.getCategoryAttributesMap().get(key), request);
                        out.add(wrapper);
                    }
                }
                return out;
            }
        }catch (RuntimeException e) {
            logger.error("Error fetching attributes of the category");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
        }

    }
}
