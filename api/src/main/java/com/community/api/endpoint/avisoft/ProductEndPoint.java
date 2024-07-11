package com.community.api.endpoint.avisoft;
import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;
import org.broadleafcommerce.core.catalog.domain.*;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.catalog.service.type.ProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/productcustom",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class ProductEndPoint extends CatalogEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ProductEndPoint.class);

    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;
    @RequestMapping(value = "getProducts/{productId}", method = RequestMethod.GET)
    public ResponseEntity<String> retrieveProductById(@PathVariable("productId") Long productId) {
        logger.debug("Retrieving product by ID: {}", productId);
        if (productId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    
        try {
            if (catalogService == null) {
                logger.error("Catalog service is not initialized.");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            final Product product = catalogService.findProductById(productId);

            if (product != null) {
                return new ResponseEntity<>(product.getName(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            logger.error("Error retrieving product: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/products", method = RequestMethod.GET)
    public List<String> retrieveAllProducts(){
        List<String> productNames = new ArrayList<>();
        try {
            if (catalogService == null) {
                logger.error("Catalog service is not initialized.");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }

            final List<Product> products = catalogService.findAllProducts();

            if (products.size() == 0) {
                logger.error("Error retrieving products as There is no product in DB");
            }
            for(Product pr: products){
                productNames.add(pr.getName());
            }
            return productNames;
        } catch (RuntimeException e) {
            logger.error("Error retrieving product");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
        }
    }

    @RequestMapping(value = "/my_product", method = RequestMethod.POST)
    public String addProduct() {

        Category category =  catalogService.createCategory();
        Long categoryId = new Long(453510);
        category.setId(categoryId);
        category.setName("test category4");
        category.setUrl("/test-category4");

        catalogService.saveCategory(category);

        Product p =  catalogService.createProduct(ProductType.PRODUCT);

        Sku newSku = catalogService.createSku();
//        Long skuId = new Long(453520);
//        newSku.setId(skuId);
        p.setDefaultSku(newSku);
        p.getDefaultSku().setDefaultProduct(p);

        p.setName("test product4");
        p.setUrl("/test-product4");
        p.setCategory(category);
        catalogService.saveProduct(p);
        return "Done";
    }


    @RequestMapping(value = "/remove/{productId}", method = RequestMethod.DELETE)
    public String removeProduct(@PathVariable("productId") Long productId){
        Product product = null;
        try {
            if (catalogService == null) {
                logger.error("Catalog service is not initialized.");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }

            product = catalogService.findProductById(productId);
            catalogService.removeProduct(product);

            if (product == null) {
                logger.error("Error deleting product as There is no product in DB with this Id");
            }
            return product.getName();
        } catch (RuntimeException e) {
            logger.error("Error deleting product");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
        }
    }

    @RequestMapping(value = "/update/{productId}", method = RequestMethod.PUT)
    public Product updateProduct(@PathVariable("productId") Long productId){
        Product product = null;
        try {
            if (catalogService == null) {
                logger.error("Catalog service is not initialized.");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }

            product = catalogService.findProductById(productId);
            catalogService.saveProduct(product);
            if (product == null) {
                logger.error("Error updating products as There is no product in DB with this Id");
            }
            return product;
        } catch (RuntimeException e) {
            logger.error("Error updating product");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
        }
    }


}