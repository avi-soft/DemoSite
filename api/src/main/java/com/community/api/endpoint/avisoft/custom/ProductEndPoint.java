package com.community.api.endpoint.avisoft.custom;
import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;
import com.community.api.endpoint.avisoft.CustomCategoryEndpoint;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.core.catalog.domain.*;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.catalog.service.type.ProductType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.*;

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

            WHAT THIS CLASS DOES FOR EACH FUNCTION WE HAVE TO THAT.

     */



    @Transactional
    @RequestMapping(value = "/add", method = RequestMethod.POST, params = {"createdDate", "expirationDate", "goLiveDate", "skuId"})
    public ResponseEntity<String> addProduct(@RequestBody ProductImpl productImpl, @RequestParam("createdDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date createdDate,
                                             @RequestParam("expirationDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date expirationDate,
                                             @RequestParam("goLiveDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date goLiveDate,
                                             @RequestParam(value = "categoryId", required = false, defaultValue = "0") Long categoryId,
                                             @RequestParam("skuId") Long skuId,
                                             @RequestParam("name") String name){

        if(categoryId != null && categoryId != 0){
            productImpl.setDefaultCategory(catalogService.findCategoryById(categoryId));
        }
        Sku sku = catalogService.findSkuById(skuId);
        if(sku == null){
            sku = catalogService.createSku();
        }

        sku.setName(name);
        sku = catalogService.saveSku(sku);
        productImpl.setDefaultSku(sku);
        productImpl.getDefaultSku().setDefaultProduct(productImpl);

        catalogService.saveSku(sku);
        Product product = catalogService.saveProduct(productImpl); // issue here is the product is saved two times.

        extProductService.saveExtProduct(createdDate, expirationDate, goLiveDate, product.getId());
        logger.info("hello" + product.getId());
        logger.info("hello2");
        return ResponseEntity.ok("good");

    }

    /*This is the function of using productImpl request body when entering the data in the blc_product.*/
    /*@Transactional
    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addProduct(@RequestBody ProductImpl productImpl){

//        logger.info("hello1" + productImpl.getId()); // before saving the id is null.
        Product product = catalogService.saveProduct(productImpl);

        logger.info("hello" + product.getId());
        return ResponseEntity.ok("good");

    }*/

    /*@Transactional
    @RequestMapping(value = "/add/{categoryName}", method = RequestMethod.POST, params = {"categoryId"})
    public ResponseEntity<String> addProduct(@RequestBody CustomProduct customProduct, @RequestParam(value = "categoryId", required = false, defaultValue = "0") Long categoryId, @RequestParam(value = "skuId", required = false, defaultValue = "0") Long skuId, @PathVariable("categoryName") String categoryName) throws ParseException {

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
            }

            // If you want to add Category if no category is found with the current categoryId.
            *//*else{
                category = this.catalogService.createCategory();
                //        category.setName("CategoryName"); // REQUIRED FOR CATEGORY TO BE CREATED
                category.setName(categoryName); // REQUIRED FOR CATEGORY TO BE CREATED
                category = catalogService.saveCategory(category);
            }*//*

            product = this.catalogService.createProduct(ProductType.PRODUCT);
            product.setDefaultCategory(category);

            Sku sku = catalogService.findSkuById(1L);
            product.setDefaultSku(sku);

            product.setMetaTitle(customProduct.getMetaTitle());

            product = catalogService.saveProduct(product);
            Date created = new Date();
            customProduct.setCreated_date(created);
            extProductService.saveExtProduct(created, customProduct.getExpiration_date(), customProduct.getGo_live_date(), product.getId());

            return ResponseEntity.ok("Data Successfully Added");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }*/


    @RequestMapping(value = "/getProducts/{productId}", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveProductById(HttpServletRequest request, @PathVariable("productId") Long productId) {

        try {

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
            response.put("metaTitle", product.getMetaTitle());
            response.put("createdDate", customProduct.getCreated_date());
            response.put("expirationDate", customProduct.getExpiration_date());
            response.put("goLiveDate", customProduct.getGo_live_date());
            // Add more fields as needed

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }

    }

    @RequestMapping(value = "/getProducts", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveProducts() {
        try {

            List<Product> products = catalogService.findAllProducts();
            if (products.size() == 0) {
                throw BroadleafWebServicesException.build(404).addMessage("No product found in the DB");
            }

            List<Map<String, Object>> responses = new ArrayList<>();
            for(Product product: products) {
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());
                if (customProduct != null) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("productId", product.getId());
                    response.put("productName", product.getName());
                    response.put("createdDate", customProduct.getCreated_date());
                    response.put("metaTitle", product.getMetaTitle());
                    response.put("expirationDate", customProduct.getExpiration_date());
                    response.put("goLiveDate", customProduct.getGo_live_date());
                    responses.add(response);
                }
            }

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }

}

