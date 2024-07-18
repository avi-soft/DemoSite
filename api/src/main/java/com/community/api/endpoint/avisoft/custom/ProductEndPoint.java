package com.community.api.endpoint.avisoft.custom;
import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;
import com.community.api.endpoint.avisoft.CustomCategoryEndpoint;
import com.community.api.services.ExceptionHandlingService;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

@RestController
@RequestMapping(value = "/productcustom",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class ProductEndPoint extends CatalogEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(CustomCategoryEndpoint.class);

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Autowired
    EntityManager entityManager;

    @Transactional
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addProduct(HttpServletRequest request, @RequestBody CustomProduct customProduct) {

        entityManager.merge(customProduct);
        return "added";
    }

    @RequestMapping(value = "getProducts/{productId}", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveProductById(HttpServletRequest request, @PathVariable("productId") Long productId) {

        if (productId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {

            if (catalogService == null) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            final CustomProduct customProduct = entityManager.find(CustomProduct.class,productId);
            System.out.println(customProduct.getExpiration_date());
            if (customProduct == null) {
                logger.error("Error retrieving product as There is no product in DB with this Id");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.categoryNotFound");
            }
            return ResponseEntity.ok(customProduct);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }

    /*@RequestMapping(value = "/products", method = RequestMethod.GET)
    public ResponseEntity<?> getAllProducts(HttpServletRequest request){

        try {

            if (catalogService == null) {
                logger.error("Catalog service is not initialized.");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }
            TypedQuery<CustomProduct> query = entityManager.createNamedQuery("CustomCategory.findAll", CustomProduct.class);
            query.setHint(QueryHints.HINT_CACHEABLE, true);
            query.setHint(QueryHints.HINT_CACHE_REGION, "query.Catalog");
            final List<CustomProduct> customProducts = query.getResultList();

            if (customProducts.isEmpty()) {
                logger.error("Error retrieving products as There is no product in DB");
                throw BroadleafWebServicesException.build(404).addMessage("com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException.productNotFound");
            }

            return ResponseEntity.ok(customProducts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }*/

    /*@RequestMapping(value = "/update/{productId}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateProduct(HttpServletRequest request, @PathVariable("productId") Long productId){

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
            return ResponseEntity.ok(wrapper);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }*/

}