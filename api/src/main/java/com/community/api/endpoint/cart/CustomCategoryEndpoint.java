package com.community.api.endpoint.cart;

import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;
import com.broadleafcommerce.rest.api.wrapper.CategoriesWrapper;
import com.broadleafcommerce.rest.api.wrapper.CategoryWrapper;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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

        CategoriesWrapper wrapper = (CategoriesWrapper)this.context.getBean(CategoriesWrapper.class.getName());
        wrapper.wrapDetails(categories, request);
        return wrapper;
    }

    @RequestMapping(value = "/categories", method = RequestMethod.GET, params = {"id"})
    public CategoryWrapper findCategoryById(HttpServletRequest request, @RequestParam("id") Long id, @RequestParam(value = "productLimit",defaultValue = "20") int productLimit, @RequestParam(value = "productOffset",defaultValue = "1") int productOffset, @RequestParam(value = "subcategoryLimit",defaultValue = "20") int subcategoryLimit, @RequestParam(value = "subcategoryOffset",defaultValue = "1") int subcategoryOffset) {
        Category cat = this.catalogService.findCategoryById(id);
        if (cat != null) {
//            request.setAttribute("productLimit", productLimit);
//            request.setAttribute("productOffset", productOffset);
//            request.setAttribute("subcategoryLimit", subcategoryLimit);
//            request.setAttribute("subcategoryOffset", subcategoryOffset);
            CategoryWrapper wrapper = (CategoryWrapper)this.context.getBean(CategoryWrapper.class.getName());
            wrapper.wrapDetails(cat, request);
            return wrapper;
        } else {
            logger.info("Categories are empty");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.categoryNotFound", id);
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String createCategory() {

    }
}
