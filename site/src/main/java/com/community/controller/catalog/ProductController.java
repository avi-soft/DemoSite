/*-
 * #%L
 * Community Demo Site
 * %%
 * Copyright (C) 2009 - 2023 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */

package com.community.controller.catalog;

import org.broadleafcommerce.common.config.service.SystemPropertiesService;
import org.broadleafcommerce.core.catalog.domain.*;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.web.catalog.ProductHandlerMapping;
import org.broadleafcommerce.core.web.controller.catalog.BroadleafProductController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * This class works in combination with the ProductHandlerMapping which finds a product based upon
 * the passed in URL.
 */
@Controller("blProductController")
public class ProductController extends BroadleafProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    protected static final String DEFAULT_PRODUCT_QUICKVIEW_PATH = "catalog/partials/productQuickView";

    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;

    @Resource(name = "blSystemPropertiesService")
    protected SystemPropertiesService systemPropertiesService;
    
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return super.handleRequest(request, response);
    }

    @RequestMapping(value = "/product-quick-view", params = {"id"})
    public ModelAndView getProductQuickView(final HttpServletRequest request, final HttpServletResponse response, 
                                            @RequestParam("id") final Long productId) throws Exception {
        final Product product = catalogService.findProductById(productId);

        request.setAttribute(ProductHandlerMapping.CURRENT_PRODUCT_ATTRIBUTE_NAME, product);
        //                                    "currentproduct"
        final ModelAndView modelAndView = super.handleRequest(request, response);
        
        modelAndView.setViewName(getProductQuickViewTemplatePath());

        return modelAndView;
    }

    @RequestMapping(value = "/product-quick-view-raman", method = RequestMethod.GET, params = {"id"})
    public Product getProductById(final HttpServletRequest request, final HttpServletResponse response,
                                  @RequestParam("id") final Long productId) throws Exception {
        Product pr = catalogService.findProductById(productId);

        logger.info("Informational message");
        logger.info(pr.getName());
        logger.info("Informational message");

        return pr;
    }

    protected String getProductQuickViewTemplatePath() {
        return systemPropertiesService.resolveSystemProperty("product.quickView.path", DEFAULT_PRODUCT_QUICKVIEW_PATH);
    }

    @RequestMapping(value = "/products", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getAllProducts() throws Exception {
        List<Product> products = catalogService.findAllProducts();
        for(Product product: products){
            logger.info(product.getName());
        }
        return "All Products Found";
    }

    @RequestMapping(value = "/product", method = RequestMethod.GET, params = {"id"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getProductById(@RequestParam("id") Long productId) throws Exception {
        Product product = catalogService.findProductById(productId);

        logger.info(product.getName());
        return "Product Found";
    }

//    Incomplete.
    @RequestMapping(value = "/product/categories", method = RequestMethod.GET, params = {"id"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getProductParentCategories(@RequestParam("id") Long productId) throws Exception {
        Product product = catalogService.findProductById(productId);

        List<Category> categories = catalogService.
//        logger.info(product.getName());
        return "Product Found";
    }
//    @RequestMapping(value = "/my_product", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
//    public ProductWrapper addCSDLProduct(HttpServletRequest request, @RequestBody ProductWrapper wrapper,
//                                         @RequestParam(value = "categoryName", required = true) String categoryName,
//                                         @RequestParam(value = "price", required = true) double price) {
//
//        Category category = null;
//        List<Category> categories = catalogService.findCategoriesByName( categoryName );
//        if ( categories != null && categories.size() > 0 ) {
//            category = categories.get(0);
//        }
//
//        Sku defaultSku = catalogService.createSku();
//        defaultSku.setRetailPrice(new Money( price ));
//        defaultSku.setInventoryType( InventoryType.ALWAYS_AVAILABLE );
//        defaultSku.setName( wrapper.getName() );
//        defaultSku.setLongDescription( wrapper.getLongDescription() );
//        defaultSku.setDescription( wrapper.getDescription() );
//        defaultSku.setUrlKey( wrapper.getUrl() );
//        defaultSku.setActiveStartDate( new Date() );
//
//        Product product = catalogService.createProduct(ProductType.PRODUCT);
//        product.setDefaultSku(defaultSku);
//        product.setUrl( wrapper.getUrl() );
//        product.setCategory(category);
//
//        List<ProductOptionXref> productOptionXrefs = new ArrayList<ProductOptionXref>();
//        List<ProductOption> allProductOptions = catalogService.readAllProductOptions();
//        if ( null != allProductOptions && allProductOptions.size() > 0 ) {
//            for ( ProductOption po : allProductOptions ) {
//                String current = po.getName();
//                if ( current.equalsIgnoreCase("Shirt Color") ) {
//                    ProductOptionXref productOptionXref = new ProductOptionXrefImpl();
//                    productOptionXref.setProductOption(po);
//                    productOptionXref.setProduct(product);
//                    productOptionXrefs.add(productOptionXref);
//                }
//            }
//        }
//
//        product.setProductOptionXrefs(productOptionXrefs);
//
//        Product finalProduct = catalogService.saveProduct(product);
//        finalProduct.getDefaultSku().setDefaultProduct(finalProduct);
//        catalogService.saveSku(finalProduct.getDefaultSku());
//        Long newId = finalProduct.getId();
//
//        ProductWrapper response;
//        response = (ProductWrapper) context.getBean(ProductWrapper.class.getName());
//        response.wrapDetails(product, request);
//        response.setId(newId);
//
//        return response;
//    }
}
