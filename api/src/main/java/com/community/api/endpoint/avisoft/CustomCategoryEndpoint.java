package com.community.api.endpoint.avisoft;

import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;
import com.broadleafcommerce.rest.api.wrapper.CategoriesWrapper;
import com.broadleafcommerce.rest.api.wrapper.CategoryWrapper;
import com.broadleafcommerce.rest.api.wrapper.ProductWrapper;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.catalog.domain.*;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.catalog.service.type.ProductType;
import org.broadleafcommerce.core.inventory.service.type.InventoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


//        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
@RestController
@RequestMapping(value = "/category")
public class CustomCategoryEndpoint extends CatalogEndpoint{

    private static final Logger logger = LoggerFactory.getLogger(CustomCategoryEndpoint.class);

    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;

    @RequestMapping(value = "/categories", method = RequestMethod.GET)
    public CategoriesWrapper findCategories(HttpServletRequest request, @RequestParam(value = "limit",defaultValue = "20") int limit) {
        List categories = this.catalogService.findAllCategories();

        if(categories.size() != 0){
            CategoriesWrapper wrapper = (CategoriesWrapper)this.context.getBean(CategoriesWrapper.class.getName());
            wrapper.wrapDetails(categories, request);
            return wrapper;
        }else{
            logger.error("Category list is empty");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.categoryNotFound");
        }
    }

    @RequestMapping(value = "", method = RequestMethod.GET, params = {"id"})
    public CategoryWrapper findCategoryById(HttpServletRequest request, @RequestParam("id") Long id, @RequestParam(value = "productLimit",defaultValue = "20") int productLimit, @RequestParam(value = "productOffset",defaultValue = "1") int productOffset, @RequestParam(value = "subcategoryLimit",defaultValue = "20") int subcategoryLimit, @RequestParam(value = "subcategoryOffset",defaultValue = "1") int subcategoryOffset) {
        Category cat = this.catalogService.findCategoryById(id);
        if (cat != null) {
            /*request.setAttribute("productLimit", productLimit);
            request.setAttribute("productOffset", productOffset);
            request.setAttribute("subcategoryLimit", subcategoryLimit);
            request.setAttribute("subcategoryOffset", subcategoryOffset);*/
            CategoryWrapper wrapper = (CategoryWrapper)this.context.getBean(CategoryWrapper.class.getName());
            wrapper.wrapDetails(cat, request);
            return wrapper;
        } else {
            logger.error("There is no category with this id");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.categoryNotFound", id);
        }
    }

    @RequestMapping(value = "/subcategories/{id}", method = RequestMethod.GET)
    public CategoriesWrapper findSubCategories(HttpServletRequest request, @PathVariable("id") Long id ){
        Category category = this.catalogService.findCategoryById(id);
        if (category != null) {
            CategoriesWrapper wrapper = (CategoriesWrapper)this.context.getBean(CategoriesWrapper.class.getName());
            List categories = this.catalogService.findAllSubCategories(category);

            wrapper.wrapDetails(categories, request);
            return wrapper;
        } else {
            logger.error("There is no category with this id to find subcategory");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.categoryNotFound", id);
        }
    }


    @RequestMapping(value = "/remove/{id}", method = RequestMethod.DELETE)
    public CategoryWrapper removeCategoryById(HttpServletRequest request, @PathVariable("id") Long id , @RequestParam(value = "productLimit",defaultValue = "20") int productLimit, @RequestParam(value = "productOffset",defaultValue = "1") int productOffset, @RequestParam(value = "subcategoryLimit",defaultValue = "20") int subcategoryLimit, @RequestParam(value = "subcategoryOffset",defaultValue = "1") int subcategoryOffset) {
        Category category = this.catalogService.findCategoryById(id);
        if (category != null) {
            catalogService.removeCategory(category);
            CategoryWrapper wrapper = (CategoryWrapper)this.context.getBean(CategoryWrapper.class.getName());
            wrapper.wrapDetails(category, request);
            return wrapper;
        } else {
            logger.error("There is no category with this id to delete");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.categoryNotFound", id);
        }
    }

    @RequestMapping(value = "/update/{id}", method = RequestMethod.PUT)
    public CategoryWrapper updateCategoryById(HttpServletRequest request, @PathVariable("id") Long id , @RequestParam(value = "productLimit",defaultValue = "20") int productLimit, @RequestParam(value = "productOffset",defaultValue = "1") int productOffset, @RequestParam(value = "subcategoryLimit",defaultValue = "20") int subcategoryLimit, @RequestParam(value = "subcategoryOffset",defaultValue = "1") int subcategoryOffset) {
        Category category = this.catalogService.findCategoryById(id);
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
    }
}
