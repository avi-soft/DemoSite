package com.community.api.endpoint.avisoft;
import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;
import com.broadleafcommerce.rest.api.wrapper.ProductWrapper;
import com.community.api.services.ExceptionHandlingService;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.catalog.service.type.ProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final Logger logger = LoggerFactory.getLogger(CustomCategoryEndpoint.class);

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

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
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            final Product product = catalogService.findProductById(productId);

            if (product != null) {
                return new ResponseEntity<>(product.getName(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/products", method = RequestMethod.GET)
    public List<ProductWrapper> getAllProducts(HttpServletRequest request){

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
            return wrappers;
        } catch (RuntimeException e) {
            throw BroadleafWebServicesException.build(404).addMessage("Error retrieving product");
        }
    }

    @RequestMapping(value = "/add/{categoryId}", method = RequestMethod.POST)
    public ProductWrapper addProduct(HttpServletRequest request, @PathVariable("categoryId") Long id) {
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
            throw BroadleafWebServicesException.build(404).addMessage("Error adding category");
        }
    }


    @RequestMapping(value = "/remove/{productId}", method = RequestMethod.DELETE)
    public ProductWrapper removeProduct(HttpServletRequest request, @PathVariable("productId") Long productId){
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
            return wrapper;
        } catch (RuntimeException e) {
            throw BroadleafWebServicesException.build(404).addMessage("Error deleting product");
        }
    }

    @RequestMapping(value = "/update/{productId}", method = RequestMethod.PUT)
    public ProductWrapper updateProduct(HttpServletRequest request, @PathVariable("productId") Long productId){
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
            return wrapper;
        } catch (RuntimeException e) {
            throw BroadleafWebServicesException.build(404).addMessage("Error updating product");
        }
    }
}