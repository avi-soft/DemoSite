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

        CategoriesWrapper wrapper = (CategoriesWrapper)this.context.getBean(CategoriesWrapper.class.getName());
        wrapper.wrapDetails(categories, request);
        return wrapper;
    }

    @RequestMapping(value = "/categories", method = RequestMethod.GET, params = {"id"})
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
            logger.info("Categories are empty");
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
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.categoryNotFound", id);
        }
    }


    /*@RequestMapping(value = "/my_product", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ProductWrapper addCSDLProduct(HttpServletRequest request, @RequestBody ProductWrapper wrapper,
                                         @RequestParam(value = "categoryName", required = true) String categoryName,
                                         @RequestParam(value = "price", required = true) double price) {

        Category category = null;
        List<Category> categories = catalogService.findCategoriesByName( categoryName );
        if ( categories != null && categories.size() > 0 ) {
            category = categories.get(0);
        }

        Sku defaultSku = catalogService.createSku();
        ((Sku) defaultSku).setRetailPrice(new Money( price ));
        defaultSku.setInventoryType( InventoryType.ALWAYS_AVAILABLE );
        defaultSku.setName( wrapper.getName() );
        defaultSku.setLongDescription( wrapper.getLongDescription() );
        defaultSku.setDescription( wrapper.getDescription() );
        defaultSku.setUrlKey( wrapper.getUrl() );
        defaultSku.setActiveStartDate( new Date() );

        Product product = catalogService.createProduct(ProductType.PRODUCT);
        ((Product) product).setDefaultSku(defaultSku);
        product.setUrl( wrapper.getUrl() );
        product.setCategory(category);

        List<ProductOptionXref> productOptionXrefs = new ArrayList<ProductOptionXref>();
        List<ProductOption> allProductOptions = catalogService.readAllProductOptions();
        if ( null != allProductOptions && allProductOptions.size() > 0 ) {
            for ( ProductOption po : allProductOptions ) {
                String current = po.getName();
                if ( current.equalsIgnoreCase("Shirt Color") ) {
                    ProductOptionXref productOptionXref = new ProductOptionXrefImpl();
                    productOptionXref.setProductOption(po);
                    productOptionXref.setProduct(product);
                    productOptionXrefs.add(productOptionXref);
                }
            }
        }

        product.setProductOptionXrefs(productOptionXrefs);

        Product finalProduct = catalogService.saveProduct(product);
        finalProduct.getDefaultSku().setDefaultProduct(finalProduct);
        catalogService.saveSku(finalProduct.getDefaultSku());
        Long newId = finalProduct.getId();

        ProductWrapper response;
        response = (ProductWrapper) context.getBean(ProductWrapper.class.getName());
        response.wrapDetails(product, request);
        response.setId(newId);

        return response;
    }*/

}
