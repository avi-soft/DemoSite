package com.community.api.endpoint.avisoft.controller.product;
import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;
import com.broadleafcommerce.rest.api.wrapper.ProductWrapper;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.catalog.service.type.ProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    public List<ProductWrapper> getAllProducts(HttpServletRequest request){

        try {
            if (catalogService == null) {
                logger.error("Catalog service is not initialized.");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }

            final List<Product> products = catalogService.findAllProducts();
            final List<ProductWrapper> wrappers = new ArrayList<>();

            if (products.size() == 0) {
                logger.error("Error retrieving products as There is no product in DB");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }

            for(Product pr: products){
                ProductWrapper wrapper = (ProductWrapper) this.context.getBean(ProductWrapper.class.getName());
                wrapper.wrapDetails(pr, request);
                wrappers.add(wrapper);
            }
            return wrappers;
        } catch (RuntimeException e) {
            logger.error("Error retrieving product");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
        }
    }

    @RequestMapping(value = "/add/{categoryId}", method = RequestMethod.POST)
    public ProductWrapper addProduct(HttpServletRequest request, @PathVariable("categoryId") Long id) {
        try {
            if (catalogService == null) {
                logger.error("Catalog service is not initialized.");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }

            if(id == null){
                logger.error("No Id is given in the Path");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
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

            /*Sku newSku = catalogService.createSku();
//        Long skuId = new Long(695);
//        newSku.setId(skuId);*/

            Sku sku = catalogService.findSkuById(695L);
            product.setDefaultSku(sku);
            product.getDefaultSku().setDefaultProduct(product);

            product.setName("test product5");
            product.setUrl("/test-product5");
            product.setCategory(category);
            catalogService.saveProduct(product);

            ProductWrapper wrapper = (ProductWrapper) this.context.getBean(ProductWrapper.class.getName());
            wrapper.wrapDetails(product, request);
            return wrapper;

        }catch (RuntimeException e) {
            logger.error("Error adding category");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
        }
    }


    @RequestMapping(value = "/remove/{productId}", method = RequestMethod.DELETE)
    public ProductWrapper removeProduct(HttpServletRequest request, @PathVariable("productId") Long productId){
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
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }
            ProductWrapper wrapper = (ProductWrapper) this.context.getBean(ProductWrapper.class.getName());
            wrapper.wrapDetails(product,request);
            return wrapper;
        } catch (RuntimeException e) {
            logger.error("Error deleting product");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
        }
    }

    @RequestMapping(value = "/update/{productId}", method = RequestMethod.PUT)
    public ProductWrapper updateProduct(HttpServletRequest request, @PathVariable("productId") Long productId){
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
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }
            product.setName("Updated testProduct 5");

            ProductWrapper wrapper = (ProductWrapper) this.context.getBean(ProductWrapper.class.getName());
            wrapper.wrapDetails(product,request);
            return wrapper;
        } catch (RuntimeException e) {
            logger.error("Error updating product");
            throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
        }
    }

}