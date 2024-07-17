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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
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
    EntityManager entityManager;

    @Transactional
    @RequestMapping(value = "/add/{categoryId}", method = RequestMethod.POST)
    public String addProduct(HttpServletRequest request, @PathVariable("categoryId") Long id) throws ParseException {

        String createdDate = "2024-07-14";
        String expirationDate = "2024-07-21";
        String goLiveDate = "2024-07-18";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        CustomProduct customProduct = new CustomProduct();
        customProduct.setCreated_date(sdf.parse(createdDate));
        customProduct.setExpiration_date(sdf.parse(expirationDate));
        customProduct.setGo_live_date(sdf.parse(goLiveDate));
        customProduct.setId(450L);
        entityManager.merge(customProduct);
        return "added";

    }

    /*@RequestMapping(value = "/remove/{productId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeProduct(HttpServletRequest request, @PathVariable("productId") Long productId){

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