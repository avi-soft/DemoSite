package com.community.api.endpoint.avisoft.controller.product;

import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddProductDto;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.dto.CustomProductWrapper;

import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomJobGroup;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductState;

import com.community.api.entity.Role;
import com.community.api.services.ResponseService;
import org.broadleafcommerce.common.persistence.Status;

import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.Sku;

import org.broadleafcommerce.core.catalog.service.type.ProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.community.api.services.JobGroupService;
import com.community.api.services.ProductService;
import com.community.api.services.RoleService;
import com.community.api.services.ReserveCategoryService;
import com.community.api.services.ProductReserveCategoryBornBeforeAfterRefService;
import com.community.api.services.ProductReserveCategoryFeePostRefService;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.services.ProductStateService;
import com.community.api.services.ApplicationScopeService;
import com.community.api.services.ReserveCategoryDtoService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.community.api.component.Constant.*;

    /*

            WHAT THIS CLASS DOES FOR EACH FUNCTION WE HAVE.

     */

@RestController
@RequestMapping(value = "/product-custom", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
public class ProductController extends CatalogEndpoint {

    public static final String TENTATIVEDATEAFTERACTIVEENDDATE = "BOTH TENTATIVE EXAMINATION DATA MUST BE AFTER ACTIVE END DATE";
    public static final String TENTATIVEEXAMDATETOAFTEREXAMDATEFROM = "TENTATIVE EXAM DATE TO MUST BE EITHER EQUAL OR BEFORE OF TENTATIVE EXAM DATE FROM";
    public static final String TENTATIVEEXAMDATEAFTERACTIVEENDDATE = "TENTATIVE EXAMINATION DATE MUST BE AFTER ACTIVE END DATE";
    public static final String POSTLESSTHANORZERO = "NUMBER OF POST CANNOT BE LESS THAN OR EQUAL TO ZERO";
    public static final String PRODUCTNOTFOUND = "PRODUCT NOT FOUND";
    public static final String PRODUCTFOUNDSUCCESSFULLY = "PRODUCTS FOUND SUCCESSFULLY";

    private final ExceptionHandlingService exceptionHandlingService;
    private final EntityManager entityManager;
    private final JwtUtil jwtTokenUtil;
    private final ProductService productService;
    private final RoleService roleService;
    private final JobGroupService jobGroupService;
    private final ProductStateService productStateService;
    private final ApplicationScopeService applicationScopeService;
    private final ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;
    private final ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;
    private final ReserveCategoryService reserveCategoryService;
    private final ReserveCategoryDtoService reserveCategoryDtoService;

    @Autowired
    public ProductController(ExceptionHandlingService exceptionHandlingService, EntityManager entityManager, JwtUtil jwtTokenUtil, ProductService productService, RoleService roleService, JobGroupService jobGroupService, ProductStateService productStateService, ApplicationScopeService applicationScopeService, ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService, ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService, ReserveCategoryService reserveCategoryService, ReserveCategoryDtoService reserveCategoryDtoService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.entityManager = entityManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.productService = productService;
        this.roleService = roleService;
        this.jobGroupService = jobGroupService;
        this.productStateService = productStateService;
        this.applicationScopeService = applicationScopeService;
        this.productReserveCategoryBornBeforeAfterRefService = productReserveCategoryBornBeforeAfterRefService;
        this.productReserveCategoryFeePostRefService = productReserveCategoryFeePostRefService;
        this.reserveCategoryService = reserveCategoryService;
        this.reserveCategoryDtoService = reserveCategoryDtoService;
    }

    @Transactional
    @PostMapping("/add/{categoryId}")
    public ResponseEntity<?> addProduct(HttpServletRequest request, @RequestBody AddProductDto addProductDto, @PathVariable Long categoryId, @RequestHeader(value = "Authorization") String authHeader) {

        try {

            if (!productService.addProductAccessAuthorisation(authHeader)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO ADD PRODUCT", HttpStatus.FORBIDDEN);
            }

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Category category = productService.validateCategory(categoryId);
            if (category == null || ((Status) category).getArchived() != 'Y') {
                ResponseService.generateErrorResponse("CATEGORY NOT FOUND", HttpStatus.NOT_FOUND);
            }

            productService.addProductDtoValidation(addProductDto);

            Product product = catalogService.createProduct(ProductType.PRODUCT);

            product.setMetaTitle(addProductDto.getMetaTitle()); // Also adding the same metaTitle in the sku.name as this will generate the auto-url.
            product.setDisplayTemplate(addProductDto.getMetaTitle());
            product.setMetaDescription(addProductDto.getMetaDescription());

            product.setDefaultCategory(category); // This is Deprecated.
            product.setCategory(category); // This will add both categoryId and productId to category_product_xref table.

            product = catalogService.saveProduct(product); // Save or update the product with values from requestBody.

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date activeStartDate = dateFormat.parse(formattedDate);

            Sku sku = catalogService.createSku(); // Create a new Sku Object
            sku.setActiveStartDate(activeStartDate);
            sku.setName(addProductDto.getMetaTitle());
            sku.setQuantityAvailable(addProductDto.getQuantity());
            sku.setDescription(addProductDto.getMetaDescription());
            sku.setActiveEndDate(addProductDto.getActiveEndDate());
            sku.setDefaultProduct(product);
//            catalogService.saveSku(sku); // this was creating duplicate entries in the db.

            CustomJobGroup customJobGroup = productService.validateCustomJobGroup(addProductDto.getJobGroup());
            if (customJobGroup == null) {
                ResponseService.generateErrorResponse("CUSTOM JOB GROUP NOT FOUND", HttpStatus.NOT_FOUND);
            }

            CustomProductState customProductState = productStateService.getProductStateByName(PRODUCT_STATE_NEW);
            if (customProductState == null) {
                ResponseService.generateErrorResponse("CUSTOM PRODUCT STATE NOT FOUND", HttpStatus.NOT_FOUND);
            }

            product.setDefaultSku(sku); // Set default SKU in the product

            productService.validateReserveCategory(addProductDto);

            Role role = productService.getRoleByToken(authHeader);
            Long creatorUserId = productService.getUserIdByToken(authHeader);

            productService.saveCustomProduct(product, addProductDto, customProductState, role, creatorUserId, product.getActiveStartDate()); // Save external product with provided dates and get status code
            productReserveCategoryFeePostRefService.saveFeeAndPost(addProductDto.getReservedCategory(), product);
            productReserveCategoryBornBeforeAfterRefService.saveBornBeforeAndBornAfter(addProductDto.getReservedCategory(), product);

            CustomProductWrapper wrapper = new CustomProductWrapper();
            CustomJobGroup jobGroup = jobGroupService.getJobGroupById(addProductDto.getJobGroup());
            CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());

            wrapper.wrapDetailsAddProduct(product, addProductDto, jobGroup, customProductState, applicationScope, creatorUserId, role, reserveCategoryService);

            return ResponseService.generateSuccessResponse("PRODUCT ADDED SUCCESSFULLY", wrapper, HttpStatus.OK);

        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            return ResponseService.generateErrorResponse(illegalArgumentException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @PutMapping("/update/{productId}")
    public ResponseEntity<?> updateProduct(HttpServletRequest request, @RequestBody AddProductDto addProductDto, @PathVariable Long productId, @RequestHeader(value = "Authorization") String authHeader) {

        try {

            if (!productService.updateProductAccessAuthorisation(authHeader, productId)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO UPDATE PRODUCT", HttpStatus.FORBIDDEN);
            }

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);
            if (customProduct == null) {
                return ResponseService.generateErrorResponse(Constant.PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }

            // Validations and checks.
            productService.validateReserveCategory(addProductDto);
            productService.deleteOldReserveCategoryMapping(customProduct);
            productService.updateProductValidation(addProductDto, customProduct);

            // Validation of getActiveEndDate and getGoLiveDate.
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date currentDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date
            customProduct.setModifiedDate(currentDate);

            productService.validateAndSetActiveEndDateAndGoLiveDateFields(addProductDto, customProduct);

            productService.validateAndSetExamDateFromAndExamDateToFields(addProductDto, customProduct);
            productService.validateExamDateFromAndExamDateTo(addProductDto, customProduct);
            productService.validateProductState(addProductDto, customProduct, authHeader);

            customProduct.setModifiedDate(new Date());
            customProduct.setModifierRole(roleService.getRoleByRoleId(jwtTokenUtil.extractRoleId(authHeader.substring(7))));
            customProduct.setModifierUserId(jwtTokenUtil.extractId(authHeader.substring(7)));

            entityManager.persist(customProduct);

            Product product = catalogService.findProductById(customProduct.getId());

            productReserveCategoryFeePostRefService.saveFeeAndPost(addProductDto.getReservedCategory(), product);
            productReserveCategoryBornBeforeAfterRefService.saveBornBeforeAndBornAfter(addProductDto.getReservedCategory(), product);

            List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(productId);

            CustomProductWrapper wrapper = new CustomProductWrapper();
            wrapper.wrapDetails(customProduct, reserveCategoryDtoList);
            return ResponseService.generateSuccessResponse("Product Updated Successfully", wrapper, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.NUMBER_FORMAT_EXCEPTION + ": " + numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/get-product-by-id/{productId}")
    public ResponseEntity<?> retrieveProductById(HttpServletRequest request, @PathVariable("productId") String productIdPath) {

        try {

            Long productId = Long.parseLong(productIdPath);
            if (productId <= 0) {
                return ResponseService.generateErrorResponse("PRODUCT ID CANNOT BE <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);

            if (customProduct == null) {
                return ResponseService.generateErrorResponse(PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }

            if ((((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {

                CustomProductWrapper wrapper = new CustomProductWrapper();

                List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(productId);
                wrapper.wrapDetails(customProduct, reserveCategoryDtoList);
                return ResponseService.generateSuccessResponse("PRODUCT FOUND", wrapper, HttpStatus.OK);

            } else {
                return ResponseService.generateErrorResponse("PRODUCT IS EITHER ARCHIVED OR EXPIRED", HttpStatus.NOT_FOUND);
            }

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.NUMBER_FORMAT_EXCEPTION + ": " + numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/get-all-products")
    public ResponseEntity<?> retrieveProducts() {

        try {

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Product> products = catalogService.findAllProducts(); // find all the products.

            if (products.isEmpty()) {
                return ResponseService.generateErrorResponse(PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }

            List<Map<String, CustomProductWrapper>> responses = new ArrayList<>();
            for (Product product : products) {

                // finding customProduct that resembles with productId.
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());

                if (customProduct != null) {

                    if ((((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {

                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct);

                        Map<String, CustomProductWrapper> productDetails = new HashMap<>();

                        productDetails.put("key_" + customProduct.getId(), wrapper);
                        productDetails.remove("key_" + customProduct.getId(), "reserveCategoryDtoList"); // gives us empty list

                        responses.add(productDetails);
                    }
                }
            }

            return ResponseService.generateSuccessResponse(PRODUCTFOUNDSUCCESSFULLY, responses, HttpStatus.OK);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable("productId") String productIdPath) {
        try {

            Long productId = Long.parseLong(productIdPath);

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId); // Find the Custom Product

            if (customProduct == null || (((Status) customProduct).getArchived() == 'Y')) {
                return ResponseService.generateErrorResponse(PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }

            catalogService.removeProduct(customProduct.getDefaultSku().getDefaultProduct()); // Make it archive from the DB.

            return ResponseService.generateSuccessResponse("PRODUCT DELETED SUCCESSFULLY", "DELETED", HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.NUMBER_FORMAT_EXCEPTION + ": " + numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-new-state-products")
    public ResponseEntity<?> getAllNewStateProducts() {

        try {

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Product> products = catalogService.findAllProducts(); // find all the products.

            if (products.isEmpty()) {
                return ResponseService.generateErrorResponse(PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }

            List<Map<String, CustomProductWrapper>> responses = new ArrayList<>();
            for (Product product : products) {

                // finding customProduct that resembles with productId.
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());

                if (customProduct != null) {

                    if ((((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date())) && customProduct.getProductState().getProductState().equals(PRODUCT_STATE_NEW)) {

                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct);

                        Map<String, CustomProductWrapper> productDetails = new HashMap<>();

                        productDetails.put("key_" + customProduct.getId(), wrapper);
                        productDetails.remove("key_" + customProduct.getId(), "reserveCategoryDtoList"); // gives us empty list

                        responses.add(productDetails);
                    }

                }
            }

            return ResponseService.generateSuccessResponse(PRODUCTFOUNDSUCCESSFULLY, responses, HttpStatus.OK);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-live-state-products")
    public ResponseEntity<?> getAllLiveStateProducts() {

        try {

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Product> products = catalogService.findAllProducts(); // find all the products.

            if (products.isEmpty()) {
                return ResponseService.generateErrorResponse(PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }

            List<Map<String, CustomProductWrapper>> responses = new ArrayList<>();
            for (Product product : products) {

                // finding customProduct that resembles with productId.
                CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());

                if (customProduct != null) {

                    if ((((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date())) && !customProduct.getGoLiveDate().after(new Date())) {

                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct);

                        Map<String, CustomProductWrapper> productDetails = new HashMap<>();

                        productDetails.put("key_" + customProduct.getId(), wrapper);
                        productDetails.remove("key_" + customProduct.getId(), "reserveCategoryDtoList"); // gives us empty list

                        responses.add(productDetails);
                    }
                }
            }

            return ResponseService.generateSuccessResponse(PRODUCTFOUNDSUCCESSFULLY, responses, HttpStatus.OK);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-filter-products")
    public ResponseEntity<?> getFilterProducts(
            @RequestParam(value = "date_from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date dateFrom,
            @RequestParam(value = "date_to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date dateTo,
            @RequestParam(value = "status", required = false) List<Long> state,
            @RequestParam(value = "category", required = false) List<Long> categories,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "fee", required = false) Double fee,
            @RequestParam(value = "post", required = false) Integer post,
            @RequestParam(value = "reserve_categories", required = false) List<Long> reserveCategories) {

        try {
            // Call the service to get filtered products
            List<CustomProduct> products = productService.filterProducts(state, categories, reserveCategories, title, fee, post, dateFrom, dateTo);

            if (products.isEmpty()) {
                return ResponseService.generateErrorResponse("NO PRODUCTS FOUND WITH THE GIVEN CRITERIA", HttpStatus.NOT_FOUND);
            }

            List<CustomProductWrapper> responses = new ArrayList<>();
            for (CustomProduct customProduct : products) {

                if (customProduct != null) {

                    if ((((Status) customProduct).getArchived() != 'Y')) {

                        CustomProductWrapper wrapper = new CustomProductWrapper();
                        wrapper.wrapDetails(customProduct);

                        responses.add(wrapper);
                    }
                }
            }

            return ResponseService.generateSuccessResponse("PRODUCTS RETRIEVED SUCCESSFULLY", responses, HttpStatus.OK);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse("SOME EXCEPTION OCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}