package com.community.api.endpoint.avisoft.custom;
import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;
import com.community.api.endpoint.avisoft.CustomCategoryEndpoint;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.catalog.service.type.ProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.broadleafcommerce.core.catalog.domain.Category;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/productcustom",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class ProductEndPoint extends CatalogEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(CustomCategoryEndpoint.class);

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected ExtProductService extProductService;

    @Autowired
    protected CatalogService catalogService;

    /*



     */

    @Transactional
    @RequestMapping(value = "/add/{categoryName}", method = RequestMethod.POST, params = {"categoryId"})
    public ResponseEntity<String> addProduct(@RequestBody CustomProduct customProduct, @RequestParam(value = "categoryId", required = false, defaultValue = "0") Long categoryId, @PathVariable("categoryName") String categoryName) throws ParseException {

        Product product = null;
        Category category = null;

        try {
            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage("Catalog service is not initialized.");
            }

            if(categoryId != null && categoryId != 0){
                category = this.catalogService.findCategoryById(categoryId);

                if(category == null){
                    throw BroadleafWebServicesException.build(404).addMessage("CategoryId not found in DB");
                }
            }else{
                category = this.catalogService.createCategory();
                //        category.setName("CategoryName"); // REQUIRED FOR CATEGORY TO BE CREATED
                category.setName(categoryName); // REQUIRED FOR CATEGORY TO BE CREATED
                category = catalogService.saveCategory(category);
            }

            product = this.catalogService.createProduct(ProductType.PRODUCT);
            product.setDefaultCategory(category);

            product = catalogService.saveProduct(product);
            Date created = new Date();
            customProduct.setCreated_date(created);
            extProductService.saveExtProduct(created, customProduct.getExpiration_date(), customProduct.getGo_live_date(), product.getId());

            return ResponseEntity.ok("Data Successfully Added");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }

    @RequestMapping(value = "getProducts", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveProducts(HttpServletRequest request, @PathVariable("productId") Long productId) {
        CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);

        if (customProduct == null) {
            return ResponseEntity.notFound().build();
        }

        // Assuming CustomProduct has a direct reference to Product
        Product product = catalogService.findProductById(productId);

        // Construct a JSON response
        Map<String, Object> response = new HashMap<>();
        response.put("productId", product.getId());
        response.put("productName", product.getName());
        response.put("createdDate", customProduct.getCreated_date());
        response.put("expirationDate", customProduct.getExpiration_date());
        response.put("goLiveDate", customProduct.getGo_live_date());
        // Add more fields as needed

        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "getProducts/{productId}", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveProductById(HttpServletRequest request, @PathVariable("productId") Long productId) {
        CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);
        return ResponseEntity.ok(customProduct.getGo_live_date());
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
    }*/

}

