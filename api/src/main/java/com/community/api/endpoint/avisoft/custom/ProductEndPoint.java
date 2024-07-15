package com.community.api.endpoint.avisoft.custom;
import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.community.api.endpoint.avisoft.CustomCategoryEndpoint;
import com.community.api.services.ExceptionHandlingService;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.catalog.service.type.ProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@RestController
@RequestMapping(value = "/productcustom",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class ProductEndPoint extends CatalogEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(CustomCategoryEndpoint.class);

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;

    @Autowired
    CustomProductService customProductService;

    @RequestMapping(value = "/add/{categoryId}", method = RequestMethod.POST)
    public String addProduct(HttpServletRequest request, @PathVariable("categoryId") Long id) throws ParseException {

        logger.info("TILL HERE1");
        Category category = catalogService.findCategoryById(id);
        if(category == null){
            category =  catalogService.createCategory();
//                Long categoryId = new Long(453510);
            category.setId(id);
            category.setName("test category4");
            category.setUrl("/test-category4");

            catalogService.saveCategory(category);
        }

        Product product =  catalogService.createProduct(ProductType.PRODUCT);

//        Sku newSku = catalogService.createSku();
//        Long skuId = new Long(695);
//        newSku.setId(skuId);

        Sku sku = catalogService.findSkuById(633L);
        product.setDefaultSku(sku);
        product.getDefaultSku().setDefaultProduct(product);

        logger.info("TILL HERE2");
        product.setName("test product5");
        product.setUrl("/test-product5");
        product.setCategory(category);

        String createdDate = "2024-07-14";
        String expirationDate = "2024-07-21";
        String goLiveDate = "2024-07-18";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        logger.info("TILL HERE3");
        CustomProduct customProduct = new CustomProduct( product, sdf.parse(createdDate),sdf.parse(expirationDate),sdf.parse(goLiveDate));
        customProductService.save(customProduct);
        return "added";
//        try {
//
//            if (catalogService == null) {
//                logger.error("Catalog service is not initialized.");
//                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
//            }
//
//            if(id == null){
//                logger.error("No Id is given in the Path");
//                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
//            }
//
//            Category category = catalogService.findCategoryById(id);
//            if(category == null){
//                category =  catalogService.createCategory();
////                Long categoryId = new Long(453510);
//                category.setId(id);
//                category.setName("test category4");
//                category.setUrl("/test-category4");
//
//                catalogService.saveCategory(category);
//            }
//
//            Product product =  catalogService.createProduct(ProductType.PRODUCT);
//
//            //Sku newSku = catalogService.createSku();
////        Long skuId = new Long(695);
////        newSku.setId(skuId);*//*
//
//            Sku sku = catalogService.findSkuById(695L);
//            product.setDefaultSku(sku);
//            product.getDefaultSku().setDefaultProduct(product);
//
//            product.setName("test product5");
//            product.setUrl("/test-product5");
//            product.setCategory(category);
//            catalogService.saveProduct(product);
//
//            ProductWrapper wrapper = (ProductWrapper) this.context.getBean(ProductWrapper.class.getName());
//            wrapper.wrapDetails(product, request);
//            return ResponseEntity.ok(wrapper);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
//        }
    }

    /*@RequestMapping(value = "getProducts/{productId}", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveProductById(HttpServletRequest request, @PathVariable("productId") Long productId) {

        if (productId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {

            if (catalogService == null) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            final Product product = catalogService.findProductById(productId);

            if (product != null) {
                ProductWrapper wrapper = (ProductWrapper)this.context.getBean(ProductWrapper.class.getName());
                wrapper.wrapDetails(product, request);
                return ResponseEntity.ok(wrapper);
            } else {
                logger.error("Error retrieving product as There is no product in DB with this Id");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.categoryNotFound");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }

    @RequestMapping(value = "/products", method = RequestMethod.GET)
    public ResponseEntity<?> getAllProducts(HttpServletRequest request){

        try {

            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            final List<Product> products = catalogService.findAllProducts();
            final List<ProductWrapper> wrappers = new ArrayList<>();

            if (products.size() == 0) {
                throw BroadleafWebServicesException.build(404).addMessage("Error retrieving products as There is no product in DB");
            }

            for(Product pr: products){
                ProductWrapper wrapper = (ProductWrapper) this.context.getBean(ProductWrapper.class.getName());
                wrapper.wrapDetails(pr, request);
                wrappers.add(wrapper);
            }

            return ResponseEntity.ok(wrappers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }

    @RequestMapping(value = "/add/{categoryId}", method = RequestMethod.POST)
    public ResponseEntity<?> addProduct(HttpServletRequest request, @PathVariable("categoryId") Long id) {

        try {

            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            if(id == null){
                throw BroadleafWebServicesException.build(404).addMessage("No Id is given in the Path");
            }

            Category category = catalogService.findCategoryById(id);
            if(category == null){
                category =  catalogService.createCategory();
//                Long categoryId = new Long(453510);
                category.setId(id);
                category.setName("test category4");
                category.setUrl("/test-category4");

                catalogService.saveCategory(category);
            }

            Product product =  catalogService.createProduct(ProductType.PRODUCT);

            *//*Sku newSku = catalogService.createSku();
//        Long skuId = new Long(695);
//        newSku.setId(skuId);*//*

            Sku sku = catalogService.findSkuById(695L);
            product.setDefaultSku(sku);
            product.getDefaultSku().setDefaultProduct(product);

            product.setName("test product5");
            product.setUrl("/test-product5");
            product.setCategory(category);
            catalogService.saveProduct(product);

            ProductWrapper wrapper = (ProductWrapper) this.context.getBean(ProductWrapper.class.getName());
            wrapper.wrapDetails(product, request);
            return ResponseEntity.ok(wrapper);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }


    @RequestMapping(value = "/remove/{productId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeProduct(HttpServletRequest request, @PathVariable("productId") Long productId){

        Product product = null;
        try {

            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            product = catalogService.findProductById(productId);
            catalogService.removeProduct(product);

            if (product == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Error deleting product as There is no product in DB with this Id");
            }

            ProductWrapper wrapper = (ProductWrapper) this.context.getBean(ProductWrapper.class.getName());
            wrapper.wrapDetails(product,request);

            return ResponseEntity.ok(wrapper);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }

    @RequestMapping(value = "/update/{productId}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateProduct(HttpServletRequest request, @PathVariable("productId") Long productId){

        Product product = null;
        try {

            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            product = catalogService.findProductById(productId);
            catalogService.saveProduct(product);
            if (product == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Error updating products as There is no product in DB with this Id");
            }

            product.setName("Updated testProduct 5");

            ProductWrapper wrapper = (ProductWrapper) this.context.getBean(ProductWrapper.class.getName());
            wrapper.wrapDetails(product,request);

            return ResponseEntity.ok(wrapper);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }*/

}