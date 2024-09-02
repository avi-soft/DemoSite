package com.community.api.endpoint.avisoft.controller.product;

import com.broadleafcommerce.rest.api.endpoint.catalog.CatalogEndpoint;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddProductDto;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.entity.*;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.services.*;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.*;
import org.broadleafcommerce.core.catalog.service.type.ProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping(value = "/productCustom",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
)
public class ProductController extends CatalogEndpoint {

    private static final String PRODUCTNOTFOUND = "Product not Found";
    private static final String CATEGORYNOTFOUND = "Category not Found";
    private static final String PRODUCTTITLENOTGIVEN = "Product MetaTitle not Given";

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected JwtUtil jwtTokenUtil;

    @Autowired
    protected ProductService productService;

    @Autowired
    protected RoleService roleService;

    @Autowired
    protected PrivilegeService privilegeService;

    @Autowired
    protected JobGroupService jobGroupService;

    @Autowired
    protected ProductStateService productStateService;

    @Autowired
    protected ApplicationScopeService applicationScopeService;

    @Autowired
    protected ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;

    @Autowired
    protected ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;

    @Autowired
    protected ReserveCategoryService reserveCategoryService;

    @Autowired
    protected ReserveCategoryDtoService reserveCategoryDtoService;

    /*

            WHAT THIS CLASS DOES FOR EACH FUNCTION WE HAVE.

     */

    @Transactional
    @PostMapping("/add/{categoryId}")
    public ResponseEntity<?> addProduct(HttpServletRequest request,
                                        @RequestBody AddProductDto addProductDto,
                                        @PathVariable Long categoryId,
                                        @RequestHeader(value = "Authorization") String authHeader) {

        try {
            String productState = null; // for now we are setting it in newState.
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();

            boolean accessGrant = false;
            Long userId = null;
            if (role.equals(Constant.SUPER_ADMIN) || role.equals(Constant.ADMIN)) {
                accessGrant = true;
                productState = Constant.PRODUCT_STATE_APPROVED;

                // -> NEED TO ADD THE USER_ID OF ADMIN OR SUPER ADMIN.

            } else if (role.equals(Constant.SERVICE_PROVIDER)) {
                userId = jwtTokenUtil.extractId(jwtToken);
                List<Privileges> privileges = privilegeService.getServiceProviderPrivilege(userId);
                for (Privileges privilege : privileges) {
                    if (privilege.getPrivilege_name().equals(Constant.PRIVILEGE_ADD_PRODUCT)) {
                        productState = Constant.PRODUCT_STATE_NEW;
                        accessGrant = true;
                        break;
                    }
                }
            }

            if (!accessGrant) {
                return new ResponseEntity<>("Not Authorized to add product", HttpStatus.INTERNAL_SERVER_ERROR);
            } // Authorization code.

            if (catalogService == null) {
                return new ResponseEntity<>(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Validations and checks.
            if (categoryId <= 0) {
                return new ResponseEntity<>("CategoryId cannot be <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Category category = catalogService.findCategoryById(categoryId);
            if (category == null) {
                return new ResponseEntity<>(CATEGORYNOTFOUND, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (addProductDto.getQuantity() != null) {
                if (addProductDto.getQuantity() <= 0) {
                    return new ResponseEntity<>("Quantity cannot be empty <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                addProductDto.setQuantity(Constant.DEFAULT_QUANTITY);
            }

            if (addProductDto.getPriorityLevel() == null) {
                addProductDto.setPriorityLevel(Constant.DEFAULT_PRIORITY_LEVEL);
            }

            Product product = catalogService.createProduct(ProductType.PRODUCT);

            product.setDefaultCategory(category); // This is Deprecated.
            product.setCategory(category); // This will add both categoryId and productId to category_product_xref table.

            if (addProductDto.getMetaTitle() == null || addProductDto.getMetaTitle().trim().isEmpty()) {
                return new ResponseEntity<>(PRODUCTTITLENOTGIVEN, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            addProductDto.setMetaTitle(addProductDto.getMetaTitle().trim());
            product.setMetaTitle(addProductDto.getMetaTitle()); // Also add the same metatTitle in the sku.name
            product.setDisplayTemplate(addProductDto.getMetaTitle());

            if (addProductDto.getMetaDescription() != null) {
                addProductDto.setMetaDescription(addProductDto.getMetaDescription().trim());
                product.setMetaDescription(addProductDto.getMetaDescription());
            }

            product = catalogService.saveProduct(product); // Save or update the product with values from requestBody.

//            // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(new Date());
            Date activeStartDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date

//            ExpirationDate and GoLiveDate VALIDATION
            if (addProductDto.getActiveEndDate() == null || addProductDto.getGoLiveDate() == null) {
                return new ResponseEntity<>("ActiveEndDate and GoLiveDate cannot be Empty", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Validation on date for being wrong types. -> these needs to be changed or we have to add exception.
            dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
            dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));

            if (!addProductDto.getActiveEndDate().after(activeStartDate)) {
                return new ResponseEntity<>("Expiration date cannot be before or equal of current date", HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (!addProductDto.getActiveEndDate().after(addProductDto.getGoLiveDate()) || !addProductDto.getGoLiveDate().after(activeStartDate)) {
                return new ResponseEntity<>("GoLive date cannot be before or equal of goLive date and before or equal of current date", HttpStatus.INTERNAL_SERVER_ERROR);
            }

//            Create a new Sku Object
            Sku sku = catalogService.createSku();

            sku.setActiveStartDate(activeStartDate);
            sku.setName(addProductDto.getMetaTitle());
            sku.setQuantityAvailable(addProductDto.getQuantity());
            sku.setDescription(addProductDto.getMetaDescription());
            sku.setActiveEndDate(addProductDto.getActiveEndDate());
            sku.setDefaultProduct(product);
            catalogService.saveSku(sku);

            // validation for new entries in the product.
            CustomJobGroup jobGroup = jobGroupService.getJobGroupById(addProductDto.getJobGroup());
            CustomProductState customProductState = productStateService.getProductStateByName(productState);

            if (addProductDto.getExamDateFrom() == null || addProductDto.getExamDateTo() == null) {
                return new ResponseEntity<>("Tentative examination date from-to cannot be null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Validation on date for being wrong types. -> these needs to be changed or we have to add exception.
            dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
            dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

            if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate()) || !addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                return new ResponseEntity<>("Both Tentative examination data must be after EndDate", HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (addProductDto.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                return new ResponseEntity<>("Tentative Exam date To must be either equal or before of Tentative Exam date From", HttpStatus.INTERNAL_SERVER_ERROR);
            }


            product.setDefaultSku(sku); // Set default SKU in the product
//            CustomProductState customProductState = productService.getCustomProductStateById(1L);

            if (addProductDto.getPlatformFee() == null) {
                return new ResponseEntity<>("Platform fee is mandatory", HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (addProductDto.getPlatformFee() <= 0) {
                return new ResponseEntity<>("Platform fee cannot be less than or equal to zero", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (addProductDto.getFee() == null) {
                return new ResponseEntity<>("Fee is mandatory", HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (addProductDto.getFee() <= 0) {
                return new ResponseEntity<>("Fee cannot be less than or equal to zero", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (addProductDto.getPost() == null) {
                addProductDto.setPost(Constant.DEFAULT_QUANTITY);
            } else if (addProductDto.getPost() <= 0) {
                return new ResponseEntity<>("Number of Post cannot be less than or equal to zero", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (addProductDto.getBornBefore() == null || addProductDto.getBornAfter() == null) {
                return new ResponseEntity<>("Born Before Date and Born After Date cannot be empty", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Validation on date for being wrong types. -> these needs to be changed or we have to add exception.
            dateFormat.parse(dateFormat.format(addProductDto.getBornAfter()));
            dateFormat.parse(dateFormat.format(addProductDto.getBornBefore()));

            if (!addProductDto.getBornBefore().before(new Date()) || !addProductDto.getBornAfter().before(new Date())) {
                return new ResponseEntity<>("Born Before Date and Born After Date must be of Past", HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (!addProductDto.getBornAfter().before(addProductDto.getBornBefore())) {
                return new ResponseEntity<>("Born After Date must be past of Born Before Date", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (addProductDto.getApplicationScope() == null) {
                return new ResponseEntity<>("Application Scope cannot be null", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());
            if (applicationScope == null) {
                return new ResponseEntity<>("No ApplicationScope exists with this Id", HttpStatus.INTERNAL_SERVER_ERROR);
            }else if(applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_STATE) && addProductDto.getNotifyingAuthority() == null){
                return new ResponseEntity<>("Notifying Authority cannot be null if ApplicationScope is: " +Constant.APPLICATION_SCOPE_STATE , HttpStatus.INTERNAL_SERVER_ERROR);
            }
            addProductDto.setNotifyingAuthority(addProductDto.getNotifyingAuthority().trim());

            productService.saveCustomProduct(product, addProductDto.getExamDateFrom(), addProductDto.getExamDateTo(), addProductDto.getGoLiveDate(), addProductDto.getPlatformFee(), addProductDto.getPriorityLevel(), applicationScope, jobGroup, customProductState, roleService.getRoleByRoleId(roleId), userId, addProductDto.getNotifyingAuthority()); // Save external product with provided dates and get status code
            productReserveCategoryBornBeforeAfterRefService.saveBornBeforeAndBornAfter(addProductDto.getBornBefore(), addProductDto.getBornAfter(), product, reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory()));
            productReserveCategoryFeePostRefService.saveFeeAndPost(addProductDto.getFee(), addProductDto.getPost(), product, reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory()));

//            // Wrap and return the updated product details
            CustomProductWrapper wrapper = new CustomProductWrapper();
            wrapper.wrapDetailsAddProduct(product, addProductDto, jobGroup, customProductState, applicationScope);

            return ResponseEntity.ok(wrapper);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return new ResponseEntity<>(Constant.NUMBER_FORMAT_EXCEPTION + ": " + numberFormatException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /*@Transactional
    @PostMapping("/add/{categoryId}")
    public ResponseEntity<?> addProduct(HttpServletRequest request,
                                        @RequestBody AddProductDto addProductDto,
                                        @PathVariable Long categoryId,
                                        @RequestHeader(value = "Authorization") String authHeader) {

        try {

//            String jwtToken = authHeader.substring(7);
//
//            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
//            String role = roleService.findRoleName(roleId);
//            boolean accessGrant = false;
//
//            if(role.equals(Constant.SUPER_ADMIN) || role.equals(Constant.ADMIN)){
//                accessGrant = true;
//            }
//            else if(role.equals("SERVICE_PROVIDER")) {
//                Long userId = jwtTokenUtil.extractId(jwtToken);
//                List<Integer> privileges = privilegeService.getPrivilege(userId);
//                for(Integer apiId: privileges) {
//                    if(apiId == 1){
//                        accessGrant = true;
//                        break;
//                    }
//                }
//            }
//
//            if(!accessGrant){
//                return new ResponseEntity<>("Not Authorized to add product", HttpStatus.INTERNAL_SERVER_ERROR);
//            }

            if (catalogService == null) {
                return new ResponseEntity<>(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (categoryId <= 0) {
                return new ResponseEntity<>("CategoryId cannot be <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Category category = catalogService.findCategoryById(categoryId);
            if (category == null) {
                return new ResponseEntity<>(CATEGORYNOTFOUND, HttpStatus.INTERNAL_SERVER_ERROR);
            }

//            if (addProductDto.getCost().doubleValue() <= 0.0) {
//                return new ResponseEntity<>("Cost cannot be <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
//            }

            if (addProductDto.getQuantity() != null) {
                if (addProductDto.getQuantity() <= 0) {
                    return new ResponseEntity<>("Quantity cannot be empty <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else {
                addProductDto.setQuantity(100000);
            }

            if(addProductDto.getPriorityLevel() == null){
                addProductDto.setPriorityLevel(5);
            }

            Product product = catalogService.createProduct(ProductType.PRODUCT);

            product.setDefaultCategory(category); // This is Deprecated.
            product.setCategory(category); // This will add both categoryId and productId to category_product_xref table.

            if (addProductDto.getMetaTitle() == null || addProductDto.getMetaTitle().trim().isEmpty()) {
                return new ResponseEntity<>(PRODUCTTITLENOTGIVEN, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            product.setMetaTitle(addProductDto.getMetaTitle().trim());

            if (addProductDto.getMetaDescription() != null) {
                addProductDto.setMetaDescription(addProductDto.getMetaDescription().trim());
                product.setMetaDescription(addProductDto.getMetaDescription());
            }
            if(addProductDto.getUrl() != null){
                addProductDto.setUrl(addProductDto.getUrl().trim());
                product.setUrl(addProductDto.getUrl());
            }

            product = catalogService.saveProduct(product); // Save or update the product with values from requestBody.

//            // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(new Date());
            Date activeStartDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date

//            COMPULSORY FIELD VALIDATION
            if(addProductDto.getActiveEndDate() == null || addProductDto.getGoLiveDate() == null){
                return new ResponseEntity<>("ActiveEndDate and GoLiveDate cannot be Empty", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (!addProductDto.getActiveEndDate().after(activeStartDate)) {
                return new ResponseEntity<>("Expiration date cannot be before or equal of current date", HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (!addProductDto.getActiveEndDate().after(addProductDto.getGoLiveDate()) || !addProductDto.getGoLiveDate().after(activeStartDate)) {
                return new ResponseEntity<>("GoLive date cannot be before or equal of goLive date and before or equal of current date", HttpStatus.INTERNAL_SERVER_ERROR);
            }

//            Create a new Sku Object
            Sku sku = catalogService.createSku();
//            sku.setCost(addProductDto.getCost());

            sku.setQuantityAvailable(addProductDto.getQuantity());
            sku.setActiveStartDate(activeStartDate);
            sku.setName(addProductDto.getMetaTitle().trim());
            sku.setQuantityAvailable(addProductDto.getQuantity());
            sku.setDescription(addProductDto.getMetaDescription());
            sku.setActiveEndDate(addProductDto.getActiveEndDate());
            sku.setDefaultProduct(product);
            catalogService.saveSku(sku);

            // validation for new entries in the product.
            if(addProductDto.getJobGroup() == null || !(addProductDto.getJobGroup().equals('A') || addProductDto.getJobGroup().equals('B') || addProductDto.getJobGroup().equals('C') || addProductDto.getJobGroup().equals('D'))) {
                return new ResponseEntity<>("Product Job Group cannot be null or other than A/B/C/D", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if(addProductDto.getExamDateFrom() == null || addProductDto.getExamDateTo() == null){
                return new ResponseEntity<>("Tentative examination date from-to cannot be null", HttpStatus.INTERNAL_SERVER_ERROR);
            }else if(!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate()) || !addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                return new ResponseEntity<>("Both Tentative examination data must be after GoLiveDate", HttpStatus.INTERNAL_SERVER_ERROR);
            }else if(addProductDto.getExamDateTo().before(addProductDto.getExamDateFrom())){
                return new ResponseEntity<>("Tentative Exam date To must be either equal or before of Tentative Exam date From", HttpStatus.INTERNAL_SERVER_ERROR);
            }


            product.setDefaultSku(sku); // Set default SKU in the product
            CustomProductState customProductState = productService.getCustomProductStateById(1L);
            productService.saveCustomProduct(addProductDto.getGoLiveDate(), addProductDto.getPriorityLevel(), product.getId(), customProductState); // Save external product with provided dates and get status code

//            // Wrap and return the updated product details
            CustomProductWrapper wrapper = new CustomProductWrapper();
            wrapper.wrapDetails(product, addProductDto.getPriorityLevel(), addProductDto.getGoLiveDate());

            return ResponseEntity.ok(wrapper);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return new ResponseEntity<>(Constant.NUMBER_FORMAT_EXCEPTION + ": " + numberFormatException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @Transactional
    @PutMapping("/update/{productId}")
    public ResponseEntity<?> updateProduct(HttpServletRequest request,
                                           @RequestBody AddProductDto addProductDto,
                                           @PathVariable Long productId,
                                           @RequestHeader(value = "Authorization") String authHeader) {

        try {

            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String role = roleService.findRoleName(roleId);
            boolean accessGrant = false;

            if (role.equals(Constant.SUPER_ADMIN) || role.equals(Constant.ADMIN)) {
                accessGrant = true;
            }

            if (!accessGrant) {
                return new ResponseEntity<>("Not Authorized to add product", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (catalogService == null) {
                return new ResponseEntity<>(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (productId <= 0) {
                return new ResponseEntity<>("ProductId cannot be <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Product product = catalogService.findProductById(productId);
            if (product == null) {
                return new ResponseEntity<>(PRODUCTNOTFOUND, HttpStatus.INTERNAL_SERVER_ERROR);
            }


            /*if (addProductDto.getCost() != null) {

                if(addProductDto.getCost().doubleValue() <= 0.0){
                    return new ResponseEntity<>("Cost cannot be <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                product.getDefaultSku().setCost(addProductDto.getCost());
            }*/

            if (addProductDto.getQuantity() != null) {
                if (addProductDto.getQuantity() <= 0) {
                    return new ResponseEntity<>("Quantity cannot be <= 0", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                product.getDefaultSku().setQuantityAvailable(addProductDto.getQuantity());
            }

            if (addProductDto.getMetaTitle() != null) {
                if (addProductDto.getMetaTitle().trim().isEmpty()) {
                    return new ResponseEntity<>(PRODUCTTITLENOTGIVEN, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                product.setMetaTitle(addProductDto.getMetaTitle().trim());
                product.setName(addProductDto.getMetaTitle().trim());
            }

            if (addProductDto.getMetaDescription() != null) {
                addProductDto.setMetaDescription(addProductDto.getMetaDescription().trim());
                product.setMetaDescription(addProductDto.getMetaDescription());
            }

//            product = catalogService.saveProduct(product); // Save or update the product with values from requestBody.
            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);

            if (addProductDto.getPriorityLevel() != null) {
                customProduct.setPriorityLevel(addProductDto.getPriorityLevel());
            }

            if (addProductDto.getActiveEndDate() != null && addProductDto.getGoLiveDate() != null) {
                if (!addProductDto.getActiveEndDate().after(customProduct.getActiveStartDate())) {
                    return new ResponseEntity<>("Expiration date cannot be before or equal of current date", HttpStatus.INTERNAL_SERVER_ERROR);
                } else if (!addProductDto.getActiveEndDate().after(addProductDto.getGoLiveDate()) || !addProductDto.getGoLiveDate().after(customProduct.getActiveStartDate())) {
                    return new ResponseEntity<>("Expiration date cannot be before or equal of goLive date and before or equal of current date", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                product.setActiveEndDate(addProductDto.getActiveEndDate());
                customProduct.setGoLiveDate(addProductDto.getGoLiveDate());
            } else if (addProductDto.getGoLiveDate() != null) {
                if (!addProductDto.getGoLiveDate().after(customProduct.getActiveStartDate())) {
                    return new ResponseEntity<>("GoLive date cannot be before or equal of activeStartDate", HttpStatus.INTERNAL_SERVER_ERROR);
                } else if (!customProduct.getActiveEndDate().after(addProductDto.getGoLiveDate()) || !addProductDto.getGoLiveDate().after(product.getActiveStartDate())) {
                    return new ResponseEntity<>("GoLive date cannot be before or equal of goLive date and before or equal of current date", HttpStatus.INTERNAL_SERVER_ERROR);
                }

                customProduct.setGoLiveDate(addProductDto.getGoLiveDate());
            } else if (addProductDto.getActiveEndDate() != null) {
                if (!addProductDto.getActiveEndDate().after(customProduct.getActiveStartDate())) {
                    return new ResponseEntity<>("Expiration date cannot be before or equal of activeStartDate", HttpStatus.INTERNAL_SERVER_ERROR);
                } else if (!addProductDto.getActiveEndDate().after(customProduct.getGoLiveDate()) || !customProduct.getGoLiveDate().after(customProduct.getActiveStartDate())) {
                    return new ResponseEntity<>("Expiration date cannot be before or equal of goLive date and before or equal of current date", HttpStatus.INTERNAL_SERVER_ERROR);
                }

                product.setActiveEndDate(addProductDto.getActiveEndDate());
            }

            catalogService.saveProduct(product);
            entityManager.merge(customProduct);

            // Wrap and return the updated product details
            CustomProductWrapper wrapper = new CustomProductWrapper();
            wrapper.wrapDetails(entityManager.find(CustomProduct.class, productId));

            return ResponseEntity.ok(wrapper);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return new ResponseEntity<>(Constant.NUMBER_FORMAT_EXCEPTION + ": " + numberFormatException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
                return new ResponseEntity<>(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);

            if (customProduct == null) {
                return new ResponseEntity<>(PRODUCTNOTFOUND, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if ((((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {

                //             Wrap and return the updated product details
                CustomProductWrapper wrapper = new CustomProductWrapper();

                List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(productId);
                wrapper.wrapDetails(customProduct, reserveCategoryDtoList);
                return ResponseEntity.ok(wrapper);

            } else {
                return ResponseEntity.ok("Product is either Archived or Expired");
            }

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return new ResponseEntity<>(Constant.NUMBER_FORMAT_EXCEPTION + ": " + numberFormatException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/getAllProducts")
    public ResponseEntity<?> retrieveProducts() {

        try {

            if (catalogService == null) {
                return new ResponseEntity<>(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Product> products = catalogService.findAllProducts(); // find all the products.

            if (products.isEmpty()) {
                return new ResponseEntity<>("product not found", HttpStatus.INTERNAL_SERVER_ERROR);
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

                        productDetails.put("key_"+customProduct.getId(), wrapper);
                        productDetails.get("key_"+customProduct.getId()).setReserveCategoryDtoList(null);

                        responses.add(productDetails);
                    }
                }
            }

            return ResponseEntity.ok(responses);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable("productId") String productIdPath) {
        try {

            Long productId = Long.parseLong(productIdPath);

            if (catalogService == null) {
                return new ResponseEntity<>(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId); // Find the Custom Product

            if (customProduct == null) {
                return new ResponseEntity<>(PRODUCTNOTFOUND, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            catalogService.removeProduct(customProduct.getDefaultSku().getDefaultProduct()); // Make it archive from the DB.

            return ResponseEntity.ok("Product Deleted Successfully");

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return new ResponseEntity<>(Constant.NUMBER_FORMAT_EXCEPTION + ": " + numberFormatException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
