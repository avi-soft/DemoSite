package com.community.api.endpoint.avisoft.custom;
import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.catalog.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping(value = "/productCustom",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class CustomProductController extends CatalogEndpoint {

    private static final String CATALOGSERVICENOTINITIALIZED = "Catalog service is not initialized.";
    private static final String PRODUCTNOTFOUND = "Product not Found";
    private static final String CATEGORYNOTFOUND = "Category not Found";
    private static final String PRODUCTTITLENOTGIVEN = "Product MetaTitle not Given";

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected CustomProductService customProductService;

    /*

            WHAT THIS CLASS DOES FOR EACH FUNCTION WE HAVE TO THAT.

     */


    @Transactional
    @PostMapping("/add")
    public ResponseEntity<String> addProduct(@RequestBody ProductImpl productImpl,
                                             @RequestParam(value = "expirationDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date activeEndDate,
                                             @RequestParam(value = "goLiveDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date goLiveDate,
                                             @RequestParam(value = "priorityLevel", required = false, defaultValue = "5") Integer priorityLevel,
                                             @RequestParam(value = "categoryId", required = true) Long categoryId,
                                             @RequestParam(value = "skuId", required = false, defaultValue = "0") Long skuId,
                                             @RequestParam(value = "quantity", required = false, defaultValue = "100000") Integer quantity,
                                             @RequestParam(value = "cost", required = true)Double cost){
        try {

            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage(CATALOGSERVICENOTINITIALIZED);
            }

            // Set default category if provided else default Category will be null (which is deprecated as well)
            if (categoryId != 0) {

                Category category = catalogService.findCategoryById(categoryId);
                if (category == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException(CATEGORYNOTFOUND)));
                }

                productImpl.setDefaultCategory(category); // This is Deprecated.
                productImpl.setCategory(category); // This will add both categoryId and productId to category_product_xref table.

            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException(CATEGORYNOTFOUND)));
            }


            //Here we check wheather the metaTitle of product is given or not in responseBody
            if(productImpl.getMetaTitle() == null || Objects.equals(productImpl.getMetaTitle(), "")){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException(PRODUCTTITLENOTGIVEN)));
            }


            // Save or update the product with values from requestBody.
            Product product = catalogService.saveProduct(productImpl);

            // Find or create the SKU
            Sku sku = catalogService.findSkuById(skuId);
            if (sku == null) {
                sku = catalogService.createSku();
                sku.setCost(new Money(cost));
                sku.setQuantityAvailable(quantity);
            }

            // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(new Date());
            Date activeStartDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date

            sku.setActiveStartDate(activeStartDate);
            sku.setActiveEndDate(activeEndDate);
            sku.setDefaultProduct(product);
            catalogService.saveSku(sku);

            // Set default SKU in the product
            product.setDefaultSku(sku);

            // Save external product with provided dates and get status code
            customProductService.saveCustomProduct(goLiveDate, priorityLevel, product.getId());

            return ResponseEntity.ok("Product added successfully");

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(HttpStatus.INTERNAL_SERVER_ERROR ,e));
        }

    }

    @GetMapping("/getProducts/{productId}")
    public ResponseEntity<?> retrieveProductById(@PathVariable("productId") Long productId) {

        try {

            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage(CATALOGSERVICENOTINITIALIZED);
            }

            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);

            if(customProduct == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException(CATEGORYNOTFOUND)));
            }

            // Assuming CustomProduct has a direct reference to Product
            catalogService.findProductById(productId);

            // Construct a JSON response
            Map<String, Object> response = new HashMap<>();
            response.put("productId", customProduct.getId());
            response.put("archived", customProduct.getArchived());
            response.put("metaTitle", customProduct.getMetaTitle());
            response.put("cost", customProduct.getDefaultSku().getCost().doubleValue());
            response.put("defaultCategoryId", customProduct.getDefaultCategory().getId());
            response.put("categoryName", customProduct.getDefaultCategory().getName());
            response.put("ActiveCreatedDate", customProduct.getDefaultSku().getActiveStartDate());
            response.put("ActiveExpirationDate", customProduct.getDefaultSku().getActiveEndDate());
            response.put("goLiveDate", customProduct.getGoLiveDate());
            response.put("priorityLevel", customProduct.getPriorityLevel());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }

    }

    @GetMapping("/getProducts")
    public ResponseEntity<?> retrieveProducts() {

        try {

            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage(CATALOGSERVICENOTINITIALIZED);
            }

            // find all the products.
            List<Product> products = catalogService.findAllProducts();

            if (products.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException(CATEGORYNOTFOUND)));
            }

            List<Map<String, Object>> responses = new ArrayList<>();
            for(Product product: products) {
                // finding customProduct that resembles with productId.
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());

                if (customProduct != null) {

                    Map<String, Object> response = new HashMap<>();
                    response.put("productId", customProduct.getId());
                    response.put("archived", customProduct.getArchived());
                    response.put("metaTitle", customProduct.getMetaTitle());
                    response.put("cost", customProduct.getDefaultSku().getCost().doubleValue());
                    response.put("defaultCategoryId", customProduct.getDefaultCategory().getId());
                    response.put("categoryName", customProduct.getDefaultCategory().getName());
                    response.put("ActiveCreatedDate", customProduct.getDefaultSku().getActiveStartDate());
                    response.put("ActiveExpirationDate", customProduct.getDefaultSku().getActiveEndDate());
                    response.put("goLiveDate", customProduct.getGoLiveDate());
                    response.put("priorityLevel", customProduct.getPriorityLevel());

                    responses.add(response);
                }
            }

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }

    @Transactional
    @PutMapping("/update/{productId}")
    public ResponseEntity<String> updateProduct(@RequestBody ProductImpl productImpl,
                                           @RequestParam(value = "expirationDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date activeEndDate,
                                           @RequestParam(value = "goLiveDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date goLiveDate,
                                           @RequestParam(value = "priorityLevel", required = false) Integer priorityLevel,
                                           @RequestParam(value = "categoryId", required = false, defaultValue = "0") Long categoryId,
                                           @RequestParam(value = "quantity", required = false) Integer quantity,
                                           @RequestParam(value = "cost", required = false) Double cost,
                                           @PathVariable("productId") Long productId ) {

        try {

            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage(CATALOGSERVICENOTINITIALIZED);
            }

            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);

            if(customProduct == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException(PRODUCTNOTFOUND)));
            }

            // first we set the values of CustomProduct -> ext_product table.
            if(goLiveDate != null){
                customProduct.setGoLiveDate(goLiveDate);
            }
            if(priorityLevel != null) {
                customProduct.setPriorityLevel(priorityLevel);
            }
            entityManager.merge(customProduct);



            // now we will update the values of ProductImpl -> blc_product table.
            // Before that we will update the sku value if any in the
            Product product = catalogService.findProductById(productId);
            if(categoryId != null && categoryId != 0){

                Category category = catalogService.findCategoryById(categoryId);

                if(category == null){
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException(CATEGORYNOTFOUND)));
                }

                if(product.getDefaultCategory() != category){
                    // here we will write a query to delete the previous data set from category_product_xref table and that will do the job.
                    customProductService.removeCategoryProductFromCategoryProductRefTable(product.getDefaultCategory().getId(), productId);

                    product.setDefaultCategory(category);
                    product.setCategory(category); // Little fuzzy here as i delete the entry if it exists in the table before.

                }

            }
            if(activeEndDate != null){
                product.getDefaultSku().setActiveEndDate(activeEndDate);
            }
            if(cost != null){
                product.getDefaultSku().setCost(new Money(cost));
            }
            if(quantity != null){
                product.getDefaultSku().setQuantityAvailable(quantity);
            }

            // Updated the necessary attributes.
            product.setMetaTitle(productImpl.getMetaTitle());
            product.setMetaDescription(productImpl.getMetaDescription());
            product.setUrl(productImpl.getUrl());

            catalogService.saveProduct(product);

            return ResponseEntity.ok("Product Updated Successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }

    }


    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable("productId") Long productId) {
        try {

            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage(CATALOGSERVICENOTINITIALIZED);
            }

            // Find the Custom Product
            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);

            if(customProduct == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException(PRODUCTNOTFOUND)));
            }

            // Make it archive from the DB.
            catalogService.removeProduct(customProduct.getDefaultSku().getDefaultProduct());

            return ResponseEntity.ok("Product Deleted Successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }
    }

}

