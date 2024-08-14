package com.community.api.endpoint.avisoft.controller.product;

import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.community.api.entity.CustomProduct;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.entity.CustomProductState;
import com.community.api.services.ProductService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.persistence.Status;
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
    private static final String SOMEEXCEPTIONOCCURED = "Some Exception Occurred";
    private static final String NUMBERFORMATEXCEPTION = "NumberFormatException";
    private static final String UNSUPPORTEDENCODINGEXCEPTION = "UnsupportedEncodingException";

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
    public ResponseEntity<?> addProduct(HttpServletRequest request,
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
                Map<String, String> paramMap = productService.getRequestParamBasedOnQueryString(queryString);

                priorityLevelParam = paramMap.get("priorityLevel");
                skuIdParam = paramMap.get("skuId");
                categoryIdParam = paramMap.get("categoryId");
                quantityParam = paramMap.get("quantity");
                costParam = paramMap.get("cost");
            }

            if (catalogService == null) {
                return new ResponseEntity<>(CATALOGSERVICENOTINITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Integer priorityLevel = Integer.parseInt(priorityLevelParam); // Default value given
            Double cost = Double.parseDouble(costParam);
            if (cost <= 0) {
                return new ResponseEntity<>("Cost cannot be <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Long categoryId = Long.parseLong(categoryIdParam);
            if (categoryId <= 0) {
                return new ResponseEntity<>("CategoryId cannot be <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Integer quantity = 0;
            if (quantityParam != null) {
                quantity = Integer.parseInt(quantityParam);
                if (quantity <= 0) {
                    return new ResponseEntity<>("Quantity cannot be empty <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            Long skuId = 0L;
            if (skuIdParam != null) {
                skuId = Long.parseLong(skuIdParam);
                if (skuId <= 0) {
                    return new ResponseEntity<>("SkuId cannot be empty <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            Category category = catalogService.findCategoryById(categoryId);
            if (category == null) {
                return new ResponseEntity<>(CATEGORYNOTFOUND, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            productImpl.setDefaultCategory(category); // This is Deprecated.
            productImpl.setCategory(category); // This will add both categoryId and productId to category_product_xref table.
            productImpl.getDefaultCategory();

            //Here we check whether the metaTitle of product is given or not in responseBody
            if (productImpl.getMetaTitle() == null || productImpl.getMetaTitle().trim().isEmpty()) {
                return new ResponseEntity<>(PRODUCTTITLENOTGIVEN, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            productImpl.setMetaTitle(productImpl.getMetaTitle().trim());

            if (productImpl.getMetaDescription() != null) {
                productImpl.setMetaDescription(productImpl.getMetaDescription().trim());
            }

            Product product = catalogService.saveProduct(productImpl); // Save or update the product with values from requestBody.
            Long productId = product.getId();

            // Find or create the SKU
            Sku sku = null;
            if (skuId != 0) {
                sku = catalogService.findSkuById(skuId);
            }
            if (sku == null) {
                sku = catalogService.createSku();
                sku.setCost(new Money(cost));

                if (quantity != 0) {
                    sku.setQuantityAvailable(quantity);
                }
            }

            // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(new Date());
            Date activeStartDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date

            if (!activeEndDate.after(activeStartDate)) {
                return new ResponseEntity<>("Expiration date cannot be before or equal of current date", HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (!activeEndDate.after(goLiveDate) || !goLiveDate.after(activeStartDate)) {
                return new ResponseEntity<>("Expiration date cannot be before or equal of goLive date and before or equal of current date", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            sku.setActiveStartDate(activeStartDate);
            sku.setActiveEndDate(activeEndDate);
            sku.setDefaultProduct(product);
            catalogService.saveSku(sku);

            product.setDefaultSku(sku); // Set default SKU in the product

            CustomProductState customProductState = productService.getCustomProductStateById(1L);
            productService.saveCustomProduct(goLiveDate, priorityLevel, product.getId(), customProductState); // Save external product with provided dates and get status code

            // Wrap and return the updated product details
            CustomProductWrapper wrapper = new CustomProductWrapper();
            wrapper.wrapDetails(product, priorityLevel, goLiveDate);

            return ResponseEntity.ok(wrapper);

        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            exceptionHandlingService.handleException(unsupportedEncodingException);
            return new ResponseEntity<>(UNSUPPORTEDENCODINGEXCEPTION + ": " + unsupportedEncodingException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return new ResponseEntity<>( NUMBERFORMATEXCEPTION + ": " + numberFormatException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(SOMEEXCEPTIONOCCURED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/getProductById/{productId}")
    public ResponseEntity<?> retrieveProductById(HttpServletRequest request, @PathVariable("productId") String productIdPath) {

        try {

            Long productId = Long.parseLong(productIdPath);
            if (productId <= 0) {
                return new ResponseEntity<>("productId cannot be <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (catalogService == null) {
                return new ResponseEntity<>(CATALOGSERVICENOTINITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);

            if (customProduct == null) {
                return new ResponseEntity<>(PRODUCTNOTFOUND, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if ((((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))){

    //             Wrap and return the updated product details
                CustomProductWrapper wrapper = new CustomProductWrapper();
                wrapper.wrapDetails(customProduct);
                return ResponseEntity.ok(wrapper);

            }else{
                return ResponseEntity.ok("Product is either Archived or Expired");
            }

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return new ResponseEntity<>(NUMBERFORMATEXCEPTION + ": "  + numberFormatException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(SOMEEXCEPTIONOCCURED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/getAllProducts")
    public ResponseEntity<?> retrieveProducts() {

        try {

            if (catalogService == null) {
                return new ResponseEntity<>(CATALOGSERVICENOTINITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Product> products = catalogService.findAllProducts(); // find all the products.

            if (products.isEmpty()) {
                return new ResponseEntity<>("product not found", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<CustomProductWrapper> responses = new ArrayList<>();
            for (Product product : products) {

                // finding customProduct that resembles with productId.
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());

                if (customProduct != null) {

                    if ((((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))){

                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct);
                        responses.add(wrapper);
                    }
                }
            }

            return ResponseEntity.ok(responses);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(SOMEEXCEPTIONOCCURED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @PutMapping("/update/{productId}")
    public ResponseEntity<?> updateProduct(HttpServletRequest request,
                                                @RequestBody ProductImpl productImpl,
                                                @RequestParam(value = "expirationDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date activeEndDate,
                                                @RequestParam(value = "goLiveDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date goLiveDate,
                                                @RequestParam(value = "priorityLevel", required = false) String priorityLevelParam,
                                                @RequestParam(value = "categoryId", required = false) String categoryIdParam,
                                                @RequestParam(value = "quantity", required = false) String quantityParam,
                                                @RequestParam(value = "cost", required = false) String costParam,
                                                @PathVariable("productId") String productIdParam) {

        try {

            // Get the query string from the request
            String queryString = request.getQueryString();

            if (queryString != null) {
                Map<String, String> paramMap = productService.getRequestParamBasedOnQueryString(queryString);

                priorityLevelParam = paramMap.get("priorityLevel");
                categoryIdParam = paramMap.get("categoryId");
                quantityParam = paramMap.get("quantity");
                costParam = paramMap.get("cost");
            }

            Long productId = Long.parseLong(productIdParam);

            Integer priorityLevel = 0;
            if (priorityLevelParam != null) {
                priorityLevel = Integer.parseInt(priorityLevelParam);
                Errors errors = productService.validatePriorityLevel(priorityLevel);
                if (errors.hasErrors()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionHandlingService.handleException(new IllegalArgumentException("Validation error: " + errors.getFieldError().getDefaultMessage())));
                }
            }

            Double cost = 0.00;
            if (costParam != null) {
                cost = Double.parseDouble(costParam);
                if (cost <= 0) {
                    return new ResponseEntity<>("Cost cannot be <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            Long categoryId = 0L;
            if (categoryIdParam != null) {
                categoryId = Long.parseLong(categoryIdParam);
                if (categoryId <= 0) {
                    return new ResponseEntity<>("CategoryId cannot be <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            Integer quantity = 0;
            if (quantityParam != null) {
                quantity = Integer.parseInt(quantityParam);
                if (quantity <= 0) {
                    return new ResponseEntity<>("Quantity cannot be <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            if (catalogService == null) {
                return new ResponseEntity<>(CATALOGSERVICENOTINITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);

            if (customProduct == null) {
                return new ResponseEntity<>(PRODUCTNOTFOUND, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // first we set the values of CustomProduct -> ext_product table.
            if (goLiveDate != null) {
                if (!goLiveDate.after(new Date())) {
                    return new ResponseEntity<>("GoLive date cannot be before and equal of current date", HttpStatus.INTERNAL_SERVER_ERROR);
                } else if (activeEndDate != null && !activeEndDate.after(goLiveDate)) {
                    return new ResponseEntity<>("Expiration date cannot be before of GoLiveDate", HttpStatus.INTERNAL_SERVER_ERROR);
                } else if (activeEndDate == null && !goLiveDate.before(customProduct.getDefaultSku().getActiveEndDate())) {
                    return new ResponseEntity<>("GoLive date cannot be after of ExpirationDate", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                customProduct.setGoLiveDate(goLiveDate);
            }
            if (priorityLevel != 0) {
                customProduct.setPriorityLevel(priorityLevel);
            }

            // now we will update the values of ProductImpl -> blc_product table.
            // Before that we will update the sku value if any in the
            Product product = catalogService.findProductById(productId);
            if (categoryId != 0) {

                Category category = catalogService.findCategoryById(categoryId);

                if (category == null) {
                    return new ResponseEntity<>(CATEGORYNOTFOUND, HttpStatus.INTERNAL_SERVER_ERROR);
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
                    return new ResponseEntity<>("Expiration date cannot be before of current date", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                product.getDefaultSku().setActiveEndDate(activeEndDate);
            }
            if (cost != 0) {
                product.getDefaultSku().setCost(new Money(cost));
            }
            if (quantity != 0) {
                product.getDefaultSku().setQuantityAvailable(quantity);
            }

            // Updated the necessary attributes.
            if (productImpl.getMetaTitle() != null) {
                if (productImpl.getMetaTitle().trim().isEmpty()) {
                    return new ResponseEntity<>(PRODUCTTITLENOTGIVEN, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                product.setMetaTitle(productImpl.getMetaTitle().trim());
            }

            if (productImpl.getMetaDescription() != null) {
                product.setMetaDescription(productImpl.getMetaDescription().trim());
            }
            if (productImpl.getUrl() != null) {
                product.setUrl(productImpl.getUrl());
            }
            catalogService.saveProduct(product);
            entityManager.merge(customProduct);

            // Wrap and return the updated product details
            CustomProductWrapper wrapper = new CustomProductWrapper();
            wrapper.wrapDetails(product, customProduct.getPriorityLevel(), customProduct.getGoLiveDate());

            return ResponseEntity.ok(wrapper);

        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            exceptionHandlingService.handleException(unsupportedEncodingException);
            return new ResponseEntity<>("UnsupportedEncodingException Occurred: " + unsupportedEncodingException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return new ResponseEntity<>("NumberFormatException: " + numberFormatException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(SOMEEXCEPTIONOCCURED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable("productId") String productIdPath) {
        try {

            Long productId = Long.parseLong(productIdPath);

            if (catalogService == null) {
                return new ResponseEntity<>(CATALOGSERVICENOTINITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId); // Find the Custom Product

            if (customProduct == null) {
                return new ResponseEntity<>(PRODUCTNOTFOUND, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            catalogService.removeProduct(customProduct.getDefaultSku().getDefaultProduct()); // Make it archive from the DB.

            return ResponseEntity.ok("Product Deleted Successfully");

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return new ResponseEntity<>(NUMBERFORMATEXCEPTION + ": " + numberFormatException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(SOMEEXCEPTIONOCCURED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
