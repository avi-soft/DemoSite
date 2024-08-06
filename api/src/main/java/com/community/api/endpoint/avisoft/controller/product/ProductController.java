package com.community.api.endpoint.avisoft.controller.product;

import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.broadleafcommerce.rest.api.exception.BroadleafWebServicesException;
import com.community.api.entity.CustomProduct;
import com.community.api.services.ProductService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.catalog.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping(value = "/productCustom",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
)
public class ProductController extends CatalogEndpoint {

    private static final String CATALOGSERVICENOTINITIALIZED = "Catalog service is not initialized.";
    private static final String PRODUCTNOTFOUND = "Product not Found";
    private static final String CATEGORYNOTFOUND = "Category not Found";
    private static final String PRODUCTTITLENOTGIVEN = "Product MetaTitle not Given";
    private static final String COSTCANNOTBEZERO = "Cost cannot be negative";
    private static final String QUANTITYCANNOTBEZERO = " Quantity cannot be negative";

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected ProductService productService;

    /*

            WHAT THIS CLASS DOES FOR EACH FUNCTION WE HAVE TO THAT.

     */


    @Transactional
    @PostMapping("/add")
    public ResponseEntity<String> addProduct(HttpServletRequest request,
                                             @RequestBody ProductImpl productImpl,
                                             @RequestParam(value = "expirationDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date activeEndDate,
                                             @RequestParam(value = "goLiveDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date goLiveDate,
                                             @RequestParam(value = "priorityLevel", required = false, defaultValue = "5") String priorityLevelParam,
                                             @RequestParam(value = "categoryId") String categoryIdParam,
                                             @RequestParam(value = "skuId", required = false) String skuIdParam,
                                             @RequestParam(value = "quantity", required = false) String quantityParam,
                                             @RequestParam(value = "cost") String costParam) {


        try {

            // Get the query string from the request
            String queryString = request.getQueryString();
            if (queryString != null) {
                // Split the query string by '&' to get each parameter
                String[] params = queryString.split("&");

                // Create a map to hold parameters
                Map<String, String> paramMap = new HashMap<>();

                // Process each parameter
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        String key = keyValue[0];
                        String value = keyValue[1];

                        // Encode the value to UTF-8
                        try {
                            value = URLEncoder.encode(value, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            // Handle encoding error if necessary
                        }

                        paramMap.put(key, value);
                    }
                }
                // Print each parameter for debugging
                System.out.println("Extracted Query Parameters:");
                paramMap.forEach((key, value) -> System.out.println(key + ": " + value));

            }

            Integer priorityLevel = Integer.parseInt(priorityLevelParam); // Default value given
            Double cost = Double.parseDouble(costParam);
            if (cost <= 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException("COST CANNOT BE <= 0")));
            }
            Long categoryId = Long.parseLong(categoryIdParam);
            if (categoryId <= 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException("CATEGORYID CANNOT BE <= 0")));
            }
            Integer quantity = 0;
            if(quantityParam != null){
                quantity = Integer.parseInt(quantityParam);
                if (quantity <= 0) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException("QUANTITY CANNOT BE <= 0")));
                }
            }
            Long skuId = 0L;
            if(skuIdParam != null){
                skuId = Long.parseLong(skuIdParam);
                if (skuId <= 0) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException("SKUID CANNOT BE <= 0")));
                }
            }

            Category category = catalogService.findCategoryById(categoryId);
            if (category == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException(CATEGORYNOTFOUND)));
            }

            productImpl.setDefaultCategory(category); // This is Deprecated.
            productImpl.setCategory(category); // This will add both categoryId and productId to category_product_xref table.
            productImpl.getDefaultCategory();

            //Here we check whether the metaTitle of product is given or not in responseBody
            if (productImpl.getMetaTitle() == null || Objects.equals(productImpl.getMetaTitle(), "")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException(PRODUCTTITLENOTGIVEN)));
            }
            productImpl.setMetaTitle(productImpl.getMetaTitle().trim());
            if(productImpl.getMetaTitle().isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException(PRODUCTTITLENOTGIVEN)));
            }
            if(productImpl.getMetaDescription() != null){
                productImpl.setMetaDescription(productImpl.getMetaDescription().trim());
            }

            // Save or update the product with values from requestBody.
            Product product = catalogService.saveProduct(productImpl);

            // Find or create the SKU
            Sku sku = null;
            if(skuId != 0){
                sku = catalogService.findSkuById(skuId);
            }
            if (sku == null) {
                sku = catalogService.createSku();
                sku.setCost(new Money(cost));

                if(quantity != 0){
                    sku.setQuantityAvailable(quantity);
                }
            }

            // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(new Date());
            Date activeStartDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date

            if (activeEndDate.before(activeStartDate)) {
                throw new RuntimeException("Expiration date cannot be before of current date");
            } else if (!activeEndDate.after(goLiveDate) || !goLiveDate.after(activeStartDate)) {
                throw new RuntimeException("Expiration date cannot be before or equal of goLive date and Greater and current date");
            }

            sku.setActiveStartDate(activeStartDate);
            sku.setActiveEndDate(activeEndDate);
            sku.setDefaultProduct(product);
            catalogService.saveSku(sku);

            // Set default SKU in the product
            product.setDefaultSku(sku);

            // Save external product with provided dates and get status code
            productService.saveCustomProduct(goLiveDate, priorityLevel, product.getId());

            return ResponseEntity.ok("Product added successfully");

        } catch (NumberFormatException numberFormatException) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(exceptionHandlingService.handleException(numberFormatException));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e));
        }

    }

    @GetMapping("/getProductById/{productId}")
    public ResponseEntity<?> retrieveProductById(@PathVariable("productId") String productIdPath) {

        try {

            Long productId = Long.parseLong(productIdPath);
            if (productId <= 0) {
                return new ResponseEntity<>("productId cannot be <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (catalogService == null) {
                return new ResponseEntity<>("catalogService is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);

            if (customProduct == null) {
                return new ResponseEntity<>("product not found", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Assuming CustomProduct has a direct reference to Product
            catalogService.findProductById(productId);

            // Construct a JSON response
            Map<String, Object> response = new HashMap<>();
            response.put("productId", customProduct.getId());
            response.put("archived", customProduct.getArchived());
            response.put("metaTitle", customProduct.getMetaTitle());
            response.put("metaDescription", customProduct.getMetaDescription());
            response.put("cost", customProduct.getDefaultSku().getCost().doubleValue());
            response.put("defaultCategoryId", customProduct.getDefaultCategory().getId());
            response.put("categoryName", customProduct.getDefaultCategory().getName());
            response.put("ActiveCreatedDate", customProduct.getDefaultSku().getActiveStartDate());
            response.put("ActiveExpirationDate", customProduct.getDefaultSku().getActiveEndDate());
            response.put("goLiveDate", customProduct.getGoLiveDate());
            response.put("priorityLevel", customProduct.getPriorityLevel());

            return ResponseEntity.ok(response);

        } catch (NumberFormatException numberFormatException) {
            return new ResponseEntity<>("numberFormatException", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Some Exception Occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/getAllProducts")
    public ResponseEntity<?> retrieveProducts() {

        try {

            if (catalogService == null) {
                return new ResponseEntity<>("catalogService is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // find all the products.
            List<Product> products = catalogService.findAllProducts();

            if (products.isEmpty()) {
                return new ResponseEntity<>("product not found", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Map<String, Object>> responses = new ArrayList<>();
            for (Product product : products) {
                // finding customProduct that resembles with productId.
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());

                if (customProduct != null) {

                    Map<String, Object> response = new HashMap<>();
                    response.put("productId", customProduct.getId());
                    response.put("archived", customProduct.getArchived());
                    response.put("metaTitle", customProduct.getMetaTitle());
                    response.put("metaDescription", customProduct.getMetaDescription());
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
            return new ResponseEntity<>("Some Exception Occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PutMapping("/update/{productId}")
    public ResponseEntity<String> updateProduct(@RequestBody ProductImpl productImpl,
                                                @RequestParam(value = "expirationDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date activeEndDate,
                                                @RequestParam(value = "goLiveDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date goLiveDate,
                                                @RequestParam(value = "priorityLevel", required = false) String priorityLevelParam,
                                                @RequestParam(value = "categoryId", required = false) String categoryIdParam,
                                                @RequestParam(value = "quantity", required = false) String quantityParam,
                                                @RequestParam(value = "cost", required = false) String costParam,
                                                @PathVariable("productId") String productIdParam) {

        try {

            Long productId = Long.parseLong(productIdParam);

            Integer priorityLevel = 0;
            if(priorityLevelParam != null){
                priorityLevel = Integer.parseInt(priorityLevelParam);
                Errors errors = productService.validatePriorityLevel(priorityLevel);
                if (errors.hasErrors()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new IllegalArgumentException("Validation error: " + errors.getFieldError().getDefaultMessage())));
                }
            }

            Double cost = 0.00;
            if(costParam != null) {
                cost = Double.parseDouble(costParam);
                if (cost <= 0) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException("COST CANNOT BE <= 0")));
                }
            }
            Long categoryId = 0L;
            if(categoryIdParam != null) {
                categoryId = Long.parseLong(categoryIdParam);
                if (categoryId <= 0) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException("CATEGORYID CANNOT BE <= 0")));
                }
            }
            Integer quantity = 0;
            if(quantityParam != null){
                quantity = Integer.parseInt(quantityParam);
                if (quantity <= 0) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException("QUANTITY CANNOT BE <= 0")));
                }
            }

            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage(CATALOGSERVICENOTINITIALIZED);
            }

            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);

            if (customProduct == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException(PRODUCTNOTFOUND)));
            }

            // first we set the values of CustomProduct -> ext_product table.
            if (goLiveDate != null) {
                if (goLiveDate.before(new Date())) {
                    throw new RuntimeException("GoLive date cannot be before of current date");
                } else if (activeEndDate != null && activeEndDate.before(goLiveDate)) {
                    throw new RuntimeException("Expiration date cannot be before of GoLiveDate");
                } else if(activeEndDate == null && !goLiveDate.before(customProduct.getDefaultSku().getActiveEndDate())){
                    throw new RuntimeException("Golive date cannot be after of ExpirationDate");
                }
                customProduct.setGoLiveDate(goLiveDate);
            }
            if (priorityLevel != 0) {
                customProduct.setPriorityLevel(priorityLevel);
            }

            // now we will update the values of ProductImpl -> blc_product table.
            // Before that we will update the sku value if any in the
            Product product = catalogService.findProductById(productId);
            if (categoryId != null && categoryId != 0) {

                Category category = catalogService.findCategoryById(categoryId);

                if (category == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException(CATEGORYNOTFOUND)));
                }

                if (product.getDefaultCategory() != category) {
                    // here we will write a query to delete the previous data set from category_product_xref table and that will do the job.
                    productService.removeCategoryProductFromCategoryProductRefTable(product.getDefaultCategory().getId(), productId);

                    product.setDefaultCategory(category);
                    product.setCategory(category); // Little fuzzy here as i delete the entry if it exists in the table before.

                }

            }
            if (activeEndDate != null) {
                if (activeEndDate.before(new Date())) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException("Expiration date cannot be before of current date")));
                }
                product.getDefaultSku().setActiveEndDate(activeEndDate);
            }
            if (cost != 0) {
                product.getDefaultSku().setCost(new Money(cost));
            }
            if (quantity != 0) {
                product.getDefaultSku().setQuantityAvailable(quantity);
            }

            System.out.println("HELLO");

            // Updated the necessary attributes.
            if (productImpl.getMetaTitle() != null) {
                if(productImpl.getMetaTitle().trim().isEmpty()){
                    throw new RuntimeException("Product MetaTitle not Given");
                }
                product.setMetaTitle(productImpl.getMetaTitle());
            }

            if (productImpl.getMetaDescription() != null) {
                product.setMetaDescription(productImpl.getMetaDescription().trim());
            }
            if (productImpl.getUrl() != null) {
                product.setUrl(productImpl.getUrl());
            }
            catalogService.saveProduct(product);
            entityManager.merge(customProduct);

            return ResponseEntity.ok("Product Updated Successfully");

        } catch (NumberFormatException numberFormatException) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(exceptionHandlingService.handleException(numberFormatException));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(e));
        }

    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable("productId") String productIdPath) {
        try {

            Long productId = Long.parseLong(productIdPath);

            if (catalogService == null) {
                throw BroadleafWebServicesException.build(404).addMessage(CATALOGSERVICENOTINITIALIZED);
            }

            // Find the Custom Product
            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);

            if (customProduct == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new RuntimeException(PRODUCTNOTFOUND)));
            }

            // Make it archive from the DB.
            catalogService.removeProduct(customProduct.getDefaultSku().getDefaultProduct());

            return ResponseEntity.ok("Product Deleted Successfully");

        } catch (NumberFormatException numberFormatException) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(exceptionHandlingService.handleException(numberFormatException));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionHandlingService.handleException(HttpStatus.INTERNAL_SERVER_ERROR, e));
        }
    }

}
