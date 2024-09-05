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
import com.community.api.entity.CustomProductReserveCategoryBornBeforeAfterRef;
import com.community.api.entity.CustomProductReserveCategoryFeePostRef;
import com.community.api.entity.CustomProductState;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.entity.Privileges;

import com.community.api.services.ResponseService;
import org.broadleafcommerce.common.persistence.Status;

import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.Sku;

import org.broadleafcommerce.core.catalog.service.type.ProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.community.api.services.JobGroupService;
import com.community.api.services.PrivilegeService;
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

    private final ExceptionHandlingService exceptionHandlingService;
    private final EntityManager entityManager;
    private final JwtUtil jwtTokenUtil;
    private final ProductService productService;
    private final RoleService roleService;
    private final PrivilegeService privilegeService;
    private final JobGroupService jobGroupService;
    private final ProductStateService productStateService;
    private final ApplicationScopeService applicationScopeService;
    private final ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;
    private final ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;
    private final ReserveCategoryService reserveCategoryService;
    private final ReserveCategoryDtoService reserveCategoryDtoService;

    @Autowired
    public ProductController(ExceptionHandlingService exceptionHandlingService, EntityManager entityManager, JwtUtil jwtTokenUtil, ProductService productService, RoleService roleService, PrivilegeService privilegeService, JobGroupService jobGroupService, ProductStateService productStateService, ApplicationScopeService applicationScopeService, ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService, ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService, ReserveCategoryService reserveCategoryService, ReserveCategoryDtoService reserveCategoryDtoService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.entityManager = entityManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.productService = productService;
        this.roleService = roleService;
        this.privilegeService = privilegeService;
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
            String productState = null;
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
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO ADD PRODUCT", HttpStatus.FORBIDDEN);
            }

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Validations and checks.
            if (categoryId <= 0) {
                return ResponseService.generateErrorResponse("CATEGORY ID CANNOT BE <= 0", HttpStatus.BAD_REQUEST);
            }

            Category category = catalogService.findCategoryById(categoryId);
            if (category == null) {
                return ResponseService.generateErrorResponse(Constant.CATEGORYNOTFOUND, HttpStatus.NOT_FOUND);
            }

            if (addProductDto.getQuantity() != null) {
                if (addProductDto.getQuantity() <= 0) {
                    return ResponseService.generateErrorResponse("QUANTITY CANNOT BE EMPTY <= 0", HttpStatus.BAD_REQUEST);
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
                return new ResponseEntity<>(PRODUCTTITLENOTGIVEN, HttpStatus.BAD_REQUEST);
            }
            addProductDto.setMetaTitle(addProductDto.getMetaTitle().trim());
            product.setMetaTitle(addProductDto.getMetaTitle()); // Also adding the same metaTitle in the sku.name as this will generate the auto-url.
            product.setDisplayTemplate(addProductDto.getMetaTitle());

            if (addProductDto.getMetaDescription() != null) {
                addProductDto.setMetaDescription(addProductDto.getMetaDescription().trim());
                product.setMetaDescription(addProductDto.getMetaDescription());
            }

            product = catalogService.saveProduct(product); // Save or update the product with values from requestBody.

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date activeStartDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date

            if (addProductDto.getActiveEndDate() == null || addProductDto.getGoLiveDate() == null) {
                return ResponseService.generateErrorResponse("ACTIVE END DATE AND GO LIVE DATE CANNOT BE EMPTY", HttpStatus.BAD_REQUEST);
            }

            // Validation on date for being wrong types. -> these needs to be changed or we have to add exception.
            dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
            dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));

            if (!addProductDto.getActiveEndDate().after(activeStartDate)) {
                return ResponseService.generateErrorResponse("EXPIRATION DATE CANNOT BE BEFORE OR EQUAL OF CURRENT DATE", HttpStatus.BAD_REQUEST);
            } else if (!addProductDto.getActiveEndDate().after(addProductDto.getGoLiveDate()) || !addProductDto.getGoLiveDate().after(activeStartDate)) {
                return ResponseService.generateErrorResponse("GO LIVE DATE CANNOT BE BEFORE OR EQUAL OF ACTIVE END DATE AND BEFORE OR EQUAL OF CURRENT DATE", HttpStatus.BAD_REQUEST);
            }

            Sku sku = catalogService.createSku(); // Create a new Sku Object
            sku.setActiveStartDate(activeStartDate);
            sku.setName(addProductDto.getMetaTitle());
            sku.setQuantityAvailable(addProductDto.getQuantity());
            sku.setDescription(addProductDto.getMetaDescription());
            sku.setActiveEndDate(addProductDto.getActiveEndDate());
            sku.setDefaultProduct(product);
            catalogService.saveSku(sku);

            CustomJobGroup jobGroup = jobGroupService.getJobGroupById(addProductDto.getJobGroup());
            CustomProductState customProductState = productStateService.getProductStateByName(productState);

            if (addProductDto.getExamDateFrom() == null || addProductDto.getExamDateTo() == null) {
                return ResponseService.generateErrorResponse("TENTATIVE EXAMINATION DATE FROM-TO CANNOT BE NULL", HttpStatus.BAD_REQUEST);
            }

            // Validation on date for being wrong types. -> these needs to be changed, or we have to add exception.
            dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
            dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

            if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate()) || !addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                return ResponseService.generateErrorResponse("BOTH TENTATIVE EXAMINATION DATA MUST BE AFTER ACTIVE END DATE", HttpStatus.BAD_REQUEST);
            } else if (addProductDto.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                return ResponseService.generateErrorResponse("TENTATIVE EXAM DATE TO MUST BE EITHER EQUAL OR BEFORE OF TENTATIVE EXAM DATE FROM", HttpStatus.BAD_REQUEST);
            }

            product.setDefaultSku(sku); // Set default SKU in the product

            if (addProductDto.getPlatformFee() == null) {
                return ResponseService.generateErrorResponse("PLATFORM FEE IS MANDATORY", HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (addProductDto.getPlatformFee() <= 0) {
                return ResponseService.generateErrorResponse("PLATFORM FEE CANNOT BE LESS THAN OR EQUAL TO ZERO", HttpStatus.BAD_REQUEST);
            }

            if (addProductDto.getFee() == null) {
                return ResponseService.generateErrorResponse("FEE IS MANDATORY", HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (addProductDto.getFee() <= 0) {
                return ResponseService.generateErrorResponse("FEE CANNOT BE LESS THAN OR EQUAL TO ZERO", HttpStatus.BAD_REQUEST);
            }

            if (addProductDto.getPost() == null) {
                addProductDto.setPost(Constant.DEFAULT_QUANTITY);
            } else if (addProductDto.getPost() <= 0) {
                return ResponseService.generateErrorResponse("NUMBER OF POST CANNOT BE LESS THAN OR EQUAL TO ZERO", HttpStatus.BAD_REQUEST);
            }

            if (addProductDto.getBornBefore() == null || addProductDto.getBornAfter() == null) {
                return ResponseService.generateErrorResponse("BORN BEFORE DATE AND BORN AFTER DATE CANNOT BE EMPTY", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Validation on date for being wrong types. -> these needs to be changed or we have to add exception.
            dateFormat.parse(dateFormat.format(addProductDto.getBornAfter()));
            dateFormat.parse(dateFormat.format(addProductDto.getBornBefore()));

            if (!addProductDto.getBornBefore().before(new Date()) || !addProductDto.getBornAfter().before(new Date())) {
                return ResponseService.generateErrorResponse("BORN BEFORE DATE AND BORN AFTER DATE MUST BE OF PAST", HttpStatus.BAD_REQUEST);
            } else if (!addProductDto.getBornAfter().before(addProductDto.getBornBefore())) {
                return ResponseService.generateErrorResponse("BORN AFTER DATE MUST BE PAST OF BORN BEFORE DATE", HttpStatus.BAD_REQUEST);
            }

            if (addProductDto.getApplicationScope() == null) {
                return ResponseService.generateErrorResponse("APPLICATION SCOPE CANNOT BE NULL", HttpStatus.BAD_REQUEST);
            }
            CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());
            if (applicationScope == null) {
                return ResponseService.generateErrorResponse("NO APPLICATION SCOPE EXISTS WITH THIS APPLICATION SCOPE ID", HttpStatus.NOT_FOUND);
            } else if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_STATE) && addProductDto.getNotifyingAuthority() == null) {
                return ResponseService.generateErrorResponse("NOTIFYING AUTHORITY CANNOT BE NULL IF APPLICATION SCOPE IS: " + Constant.APPLICATION_SCOPE_STATE, HttpStatus.BAD_REQUEST);
            }
            if (addProductDto.getNotifyingAuthority() != null) {
                addProductDto.setNotifyingAuthority(addProductDto.getNotifyingAuthority().trim());
            }

            if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_CENTER)) {
                addProductDto.setDomicileRequired(false);
            } else {
                if (addProductDto.getDomicileRequired() == null) {
                    return ResponseService.generateErrorResponse("APPLICATION SCOPE IS: " + applicationScope.getApplicationScope() + " DOMICILE CANNOT BE FALSE.", HttpStatus.BAD_REQUEST);
                }
            }

            if (addProductDto.getAdvertiserUrl() == null) {
                return ResponseService.generateErrorResponse("ADVERTISER URL CANNOT BE NULL", HttpStatus.BAD_REQUEST);
            } else {
                addProductDto.setAdvertiserUrl(addProductDto.getAdvertiserUrl().trim());
            }

            productService.saveCustomProduct(product, addProductDto, addProductDto.getExamDateFrom(), addProductDto.getExamDateTo(), addProductDto.getGoLiveDate(), addProductDto.getPlatformFee(), addProductDto.getPriorityLevel(), applicationScope, jobGroup, customProductState, roleService.getRoleByRoleId(roleId), userId, addProductDto.getNotifyingAuthority(), product.getActiveStartDate(), null, null); // Save external product with provided dates and get status code
            productReserveCategoryBornBeforeAfterRefService.saveBornBeforeAndBornAfter(addProductDto.getBornBefore(), addProductDto.getBornAfter(), product, reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory()));
            productReserveCategoryFeePostRefService.saveFeeAndPost(addProductDto.getFee(), addProductDto.getPost(), product, reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory()));
            CustomReserveCategory customReserveCategory = reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory());

//            // Wrap and return the updated product details
            CustomProductWrapper wrapper = new CustomProductWrapper();
            wrapper.wrapDetailsAddProduct(product, addProductDto, jobGroup, customProductState, applicationScope, customReserveCategory, userId, roleService.getRoleByRoleId(roleId));

            return ResponseService.generateSuccessResponse("PRODUCT ADDED SUCCESSFULLY", wrapper, HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.NUMBER_FORMAT_EXCEPTION + ": " + numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @PutMapping("/update/{productId}")
    public ResponseEntity<?> updateProduct(HttpServletRequest request, @RequestBody AddProductDto addProductDto, @PathVariable Long productId, @RequestHeader(value = "Authorization") String authHeader) {

        try {

            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();

            if (catalogService == null) {
                return ResponseService.generateErrorResponse(Constant.CATALOG_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (productId <= 0) {
                return ResponseService.generateErrorResponse("PRODUCT ID CANNOT BE <= 0", HttpStatus.BAD_REQUEST);
            }
            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);
            if (customProduct == null) {
                return ResponseService.generateErrorResponse(PRODUCTNOTFOUND, HttpStatus.NOT_FOUND);
            }

            boolean accessGrant = false;
            Long userId = null;
            if (role.equals(Constant.SUPER_ADMIN) || role.equals(Constant.ADMIN)) {
                accessGrant = true;

                // -> NEED TO ADD THE USER_ID OF ADMIN OR SUPER ADMIN.

            } else if (role.equals(Constant.SERVICE_PROVIDER)) {

                userId = jwtTokenUtil.extractId(jwtToken);
                if (!customProduct.getUserId().equals(userId)) {
                    List<Privileges> privileges = privilegeService.getServiceProviderPrivilege(userId);
                    for (Privileges privilege : privileges) {
                        if (privilege.getPrivilege_name().equals(Constant.PRIVILEGE_UPDATE_PRODUCT)) {
                            accessGrant = true;
                            break;
                        }
                    }
                }
            }

            if (!accessGrant) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO UPDATE PRODUCT", HttpStatus.FORBIDDEN);
            }

            // Validations and checks.
            if (addProductDto.getQuantity() != null) {
                if (addProductDto.getQuantity() <= 0) {
                    return ResponseService.generateErrorResponse("QUANTITY CANNOT BE EMPTY <= 0", HttpStatus.BAD_REQUEST);
                }
                customProduct.getDefaultSku().setQuantityAvailable(addProductDto.getQuantity());
            }

            if (addProductDto.getPriorityLevel() != null) {
                customProduct.setPriorityLevel(addProductDto.getPriorityLevel());
            }

            if (addProductDto.getMetaTitle() != null && !addProductDto.getMetaTitle().trim().isEmpty()) {
                addProductDto.setMetaTitle(addProductDto.getMetaTitle().trim());
                customProduct.setMetaTitle(addProductDto.getMetaTitle());
                customProduct.getDefaultSku().setName(addProductDto.getMetaTitle());
                customProduct.setDisplayTemplate(addProductDto.getMetaTitle());
            }
            if (addProductDto.getMetaDescription() != null && !addProductDto.getMetaDescription().trim().isEmpty()) {
                addProductDto.setMetaDescription(addProductDto.getMetaDescription().trim());
                customProduct.setMetaDescription(addProductDto.getMetaDescription());
                customProduct.getDefaultSku().setDescription(addProductDto.getMetaDescription());
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date currentDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date
            customProduct.setModifiedDate(currentDate);

            if (addProductDto.getActiveEndDate() != null && addProductDto.getGoLiveDate() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
                dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));

                if (!addProductDto.getActiveEndDate().after(customProduct.getActiveStartDate())) {
                    return ResponseService.generateErrorResponse("ACTIVE END DATE CANNOT BE BEFORE OR EQUAL OF ACTIVE START DATE", HttpStatus.BAD_REQUEST);
                } else if (!addProductDto.getActiveEndDate().after(addProductDto.getGoLiveDate()) || !addProductDto.getGoLiveDate().after(customProduct.getActiveStartDate())) {
                    return ResponseService.generateErrorResponse("GO LIVE DATE CANNOT BE BEFORE OR EQUAL OF GO LIVE DATE AND BEFORE OR EQUAL OF ACTIVE START DATE", HttpStatus.BAD_REQUEST);
                } else if (addProductDto.getExamDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));

                    if (!addProductDto.getActiveEndDate().before(addProductDto.getExamDateFrom())) {
                        return ResponseService.generateErrorResponse("ACTIVE END DATE CANNOT BE AFTER OR EQUAL OF EXAM DATE FROM DATE", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    if (!addProductDto.getActiveEndDate().before(customProduct.getExamDateFrom())) {
                        return ResponseService.generateErrorResponse("ACTIVE END DATE CANNOT BE AFTER OR EQUAL OF EXAM DATE FROM DATE", HttpStatus.BAD_REQUEST);
                    }
                }
                customProduct.getDefaultSku().setActiveEndDate(addProductDto.getActiveEndDate());
                customProduct.setGoLiveDate(addProductDto.getGoLiveDate());

            } else if (addProductDto.getActiveEndDate() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));

                if (!addProductDto.getActiveEndDate().after(customProduct.getActiveStartDate())) {
                    return ResponseService.generateErrorResponse("ACTIVE END DATE CANNOT BE BEFORE OR EQUAL OF ACTIVE START DATE", HttpStatus.BAD_REQUEST);
                } else if (!addProductDto.getActiveEndDate().after(customProduct.getGoLiveDate())) {
                    return ResponseService.generateErrorResponse("ACTIVE END DATE CANNOT BE BEFORE OR EQUAL OF GO LIVE DATE", HttpStatus.BAD_REQUEST);
                } else if (addProductDto.getExamDateFrom() != null) {

                    dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        return ResponseService.generateErrorResponse("EXAM DATE FROM MUST BE AFTER ACTIVE END DATE", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    if (!customProduct.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        return ResponseService.generateErrorResponse("EXAM DATE FROM MUST BE AFTER ACTIVE END DATE", HttpStatus.BAD_REQUEST);
                    }
                }

                customProduct.getDefaultSku().setActiveEndDate(addProductDto.getActiveEndDate());
            } else if (addProductDto.getGoLiveDate() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));

                if (!addProductDto.getGoLiveDate().after(customProduct.getActiveStartDate())) {
                    return ResponseService.generateErrorResponse("GO LIVE DATE CANNOT BE BEFORE OR EQUAL OF ACTIVE START DATE", HttpStatus.BAD_REQUEST);
                } else if (!customProduct.getActiveEndDate().after(addProductDto.getGoLiveDate())) {
                    return ResponseService.generateErrorResponse("GO LIVE DATE CANNOT BE AFTER AND EQUAL OF EXPIRY DATE", HttpStatus.BAD_REQUEST);
                }
                customProduct.setGoLiveDate(addProductDto.getGoLiveDate());
            }

            CustomJobGroup jobGroup = null;
            if (addProductDto.getJobGroup() != null) {

                jobGroup = jobGroupService.getJobGroupById(addProductDto.getJobGroup());
                if (jobGroup == null) {
                    return ResponseService.generateErrorResponse("NO JOB GROUP EXISTS WITH THIS JOB GROUP ID", HttpStatus.NOT_FOUND);
                }
                customProduct.setJobGroup(jobGroup);
            }

            if (addProductDto.getExamDateFrom() != null && addProductDto.getExamDateTo() != null) {

                // Validation on date for being wrong types. -> these needs to be changed or we have to add exception.
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

                if (addProductDto.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    return ResponseService.generateErrorResponse("TENTATIVE EXAM DATE TO MUST BE EITHER EQUAL OR BEFORE OF TENTATIVE EXAM DATE FROM", HttpStatus.BAD_REQUEST);
                }

                if (addProductDto.getActiveEndDate() != null) {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate()) || !addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        return ResponseService.generateErrorResponse("BOTH TENTATIVE EXAMINATION DATA MUST BE AFTER ACTIVE END DATE", HttpStatus.BAD_REQUEST);
                    }

                } else {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate()) || !addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        return ResponseService.generateErrorResponse("BOTH TENTATIVE EXAMINATION DATA MUST BE AFTER ACTIVE END DATE", HttpStatus.BAD_REQUEST);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());
                customProduct.setExamDateTo(addProductDto.getExamDateTo());

            } else if (addProductDto.getExamDateFrom() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                if (customProduct.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    return ResponseService.generateErrorResponse("TENTATIVE EXAM DATE TO MUST BE EITHER EQUAL OR BEFORE OF TENTATIVE EXAM DATE FROM", HttpStatus.BAD_REQUEST);
                }

                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate())) {
                        return ResponseService.generateErrorResponse("TENTATIVE EXAMINATION DATE MUST BE AFTER ACTIVE END DATE", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        return ResponseService.generateErrorResponse("TENTATIVE EXAMINATION DATE MUST BE AFTER ACTIVE END DATE", HttpStatus.BAD_REQUEST);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());

            } else if (addProductDto.getExamDateTo() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));
                if (addProductDto.getExamDateTo().before(customProduct.getExamDateFrom())) {
                    return ResponseService.generateErrorResponse("TENTATIVE EXAM DATE TO MUST BE EITHER EQUAL OR BEFORE OF TENTATIVE EXAM DATE FROM", HttpStatus.BAD_REQUEST);
                }
                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        return ResponseService.generateErrorResponse("TENTATIVE EXAMINATION DATE MUST BE AFTER ACTIVE END DATE", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    if (!addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        return ResponseService.generateErrorResponse("TENTATIVE EXAMINATION DATA MUST BE AFTER ACTIVE END DATE", HttpStatus.BAD_REQUEST);
                    }
                }
                customProduct.setExamDateTo(addProductDto.getExamDateTo());

            }

            if (addProductDto.getPlatformFee() != null) {
                if (addProductDto.getPlatformFee() <= 0) {
                    return ResponseService.generateErrorResponse("PLATFORM FEE CANNOT BE LESS THAN OR EQUAL TO ZERO", HttpStatus.BAD_REQUEST);
                }
                customProduct.setPlatformFee(addProductDto.getPlatformFee());
            }

            if (addProductDto.getApplicationScope() != null) {
                CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());
                if (applicationScope == null) {
                    return ResponseService.generateErrorResponse("NO APPLICATION SCOPE EXISTS WITH THIS ID", HttpStatus.NOT_FOUND);
                } else if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_STATE)) {
                    if (customProduct.getNotifyingAuthority() == null && addProductDto.getNotifyingAuthority() == null) {
                        return ResponseService.generateErrorResponse("NOTIFYING AUTHORITY CANNOT BE NULL IF APPLICATION SCOPE IS: " + Constant.APPLICATION_SCOPE_STATE, HttpStatus.BAD_REQUEST);
                    } else if (addProductDto.getNotifyingAuthority() != null && !addProductDto.getNotifyingAuthority().trim().isEmpty()) {
                        addProductDto.setNotifyingAuthority(addProductDto.getNotifyingAuthority().trim());
                        customProduct.setNotifyingAuthority(addProductDto.getNotifyingAuthority().trim());
                        customProduct.setCustomApplicationScope(applicationScope);
                    }
                }
            }

            if (addProductDto.getAdvertiserUrl() != null && !addProductDto.getAdvertiserUrl().trim().isEmpty()) {
                addProductDto.setAdvertiserUrl(addProductDto.getAdvertiserUrl().trim());
                customProduct.setAdvertiserUrl(addProductDto.getAdvertiserUrl());
            }

            if (addProductDto.getProductState() != null) {
                CustomProductState customProductState = productStateService.getProductStateById(addProductDto.getProductState());
                if (customProductState == null) {
                    return ResponseService.generateErrorResponse("NO PRODUCT STATE EXIST WITH THIS ID", HttpStatus.NOT_FOUND);
                }

                if (role.equals(Constant.SERVICE_PROVIDER) && customProduct.getProductState().getProductState().equals(Constant.PRODUCT_STATE_NEW) && customProductState.getProductState().equals(Constant.PRODUCT_STATE_APPROVED)) {
                    List<Privileges> privileges = privilegeService.getServiceProviderPrivilege(userId);
                    for (Privileges privilege : privileges) {
                        if (privilege.getPrivilege_name().equals(Constant.PRIVILEGE_APPROVE_PRODUCT)) {
                            customProduct.setProductState(customProductState);
                            break;
                        }
                    }
                } else if (customProduct.getProductState().getProductState().equals(Constant.PRODUCT_STATE_APPROVED) && customProductState.getProductState().equals(Constant.PRODUCT_STATE_APPROVED)) {
                    return ResponseService.generateErrorResponse("ALREADY IN APPROVED STATE", HttpStatus.BAD_REQUEST);
                } else if (role.equals(Constant.SERVICE_PROVIDER)) {
                    return ResponseService.generateErrorResponse("SERVICE PROVIDER CAN ONLY MODIFY PRODUCT STATE FROM NEW TO APPROVE", HttpStatus.FORBIDDEN);
                }
                customProduct.setProductState(customProductState);
            }

            entityManager.persist(customProduct);

            // We have to mapped the new reserveCategories.
            if (addProductDto.getReservedCategory() != null) {
                List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(productId);
                boolean reserveCategoryFound = false;
                for (ReserveCategoryDto reserveCategoryDto : reserveCategoryDtoList) {
                    if (reserveCategoryDto.getReserveCategoryId().equals(addProductDto.getReservedCategory())) {
                        reserveCategoryFound = true;
                        break;
                    }
                }

                if (reserveCategoryFound) {
                    if (addProductDto.getBornAfter() == null && addProductDto.getBornBefore() == null && addProductDto.getFee() == null && addProductDto.getPost() == null) {
                        return ResponseService.generateErrorResponse("NOTHING TO UPDATE IN RESERVE CATEGORY DATA", HttpStatus.BAD_REQUEST);
                    }

                    CustomProductReserveCategoryBornBeforeAfterRef customProductReserveCategoryBornBeforeAfterRef = productReserveCategoryBornBeforeAfterRefService.getCustomProductReserveCategoryBornBeforeAfterRefByProductIdAndReserveCategoryId(productId, addProductDto.getReservedCategory());
                    CustomProductReserveCategoryFeePostRef customProductReserveCategoryFeePostRef = productReserveCategoryFeePostRefService.getCustomProductReserveCategoryFeePostRefByProductIdAndReserveCategoryId(productId, addProductDto.getReservedCategory());

                    if (addProductDto.getBornAfter() != null && addProductDto.getBornBefore() != null) {
                        dateFormat.parse(dateFormat.format(addProductDto.getBornAfter()));
                        dateFormat.parse(dateFormat.format(addProductDto.getBornAfter()));

                        if (!addProductDto.getBornBefore().before(new Date()) || !addProductDto.getBornAfter().before(new Date())) {
                            return ResponseService.generateErrorResponse("BORN BEFORE DATE AND BORN AFTER DATE MUST BE OF PAST", HttpStatus.BAD_REQUEST);
                        } else if (!addProductDto.getBornAfter().before(addProductDto.getBornBefore())) {
                            return ResponseService.generateErrorResponse("BORN AFTER DATE MUST BE PAST OF BORN BEFORE DATE", HttpStatus.BAD_REQUEST);
                        }
                        customProductReserveCategoryBornBeforeAfterRef.setBornBefore(addProductDto.getBornBefore());
                        customProductReserveCategoryBornBeforeAfterRef.setBornAfter(addProductDto.getBornAfter());
                    } else if (addProductDto.getBornAfter() != null) {

                        dateFormat.parse(dateFormat.format(addProductDto.getBornAfter()));
                        if (!addProductDto.getBornAfter().before(new Date())) {
                            return ResponseService.generateErrorResponse("BORN AFTER DATE MUST BE OF PAST", HttpStatus.BAD_REQUEST);
                        } else if (!addProductDto.getBornAfter().before(customProductReserveCategoryBornBeforeAfterRef.getBornBefore())) {
                            return ResponseService.generateErrorResponse("BORN AFTER DATE MUST BE PAST OF BORN BEFORE DATE", HttpStatus.BAD_REQUEST);
                        }

                        customProductReserveCategoryBornBeforeAfterRef.setBornAfter(addProductDto.getBornAfter());
                    } else if (addProductDto.getBornBefore() != null) {

                        dateFormat.parse(dateFormat.format(addProductDto.getBornBefore()));
                        if (!addProductDto.getBornBefore().before(new Date())) {
                            return ResponseService.generateErrorResponse("BORN BEFORE DATE MUST BE OF PAST", HttpStatus.BAD_REQUEST);
                        } else if (!customProductReserveCategoryBornBeforeAfterRef.getBornAfter().before(addProductDto.getBornBefore())) {
                            return ResponseService.generateErrorResponse("BORN AFTER DATE MUST BE PAST OF BORN BEFORE DATE", HttpStatus.BAD_REQUEST);
                        }

                        customProductReserveCategoryBornBeforeAfterRef.setBornAfter(addProductDto.getBornAfter());
                    }
                    entityManager.persist(customProductReserveCategoryBornBeforeAfterRef);

                    if (addProductDto.getBornAfter() != null) {
                        dateFormat.parse(dateFormat.format(addProductDto.getBornAfter()));
                    }
                    if (addProductDto.getFee() != null) {
                        if (addProductDto.getFee() <= 0) {
                            return ResponseService.generateErrorResponse("FEE CANNOT BE LESS THAN OR EQUAL TO ZERO", HttpStatus.BAD_REQUEST);
                        }
                        customProductReserveCategoryFeePostRef.setFee(addProductDto.getFee());
                    }
                    if (addProductDto.getPost() != null) {
                        if (addProductDto.getPost() <= 0) {
                            return ResponseService.generateErrorResponse("NUMBER OF POST CANNOT BE LESS THAN OR EQUAL TO ZERO", HttpStatus.BAD_REQUEST);
                        }
                        customProductReserveCategoryFeePostRef.setPost(addProductDto.getPost());
                    }
                    entityManager.persist(customProductReserveCategoryFeePostRef);

                } else {
                    if (addProductDto.getFee() == null || addProductDto.getBornAfter() == null || addProductDto.getBornBefore() == null) {
                        return ResponseService.generateErrorResponse("FEE, BORN BEFORE AND BORN AFTER ARE MANDATORY FOR NEW RESERVE CATEGORY DATA", HttpStatus.BAD_REQUEST);
                    }
                    if (addProductDto.getFee() <= 0) {
                        return ResponseService.generateErrorResponse("FEE CANNOT BE LESS THAN OR EQUAL TO ZERO", HttpStatus.BAD_REQUEST);
                    }
                    if (addProductDto.getPost() == null) {
                        addProductDto.setPost(Constant.DEFAULT_QUANTITY);
                    } else if (addProductDto.getPost() <= 0) {
                        return ResponseService.generateErrorResponse("NUMBER OF POST CANNOT BE LESS THAN OR EQUAL TO ZERO", HttpStatus.BAD_REQUEST);
                    }

                    dateFormat.parse(dateFormat.format(addProductDto.getBornAfter()));
                    dateFormat.parse(dateFormat.format(addProductDto.getBornBefore()));
                    if (!addProductDto.getBornBefore().before(new Date()) || !addProductDto.getBornAfter().before(new Date())) {
                        return ResponseService.generateErrorResponse("BORN BEFORE DATE AND BORN AFTER DATE MUST BE OF PAST", HttpStatus.BAD_REQUEST);
                    } else if (!addProductDto.getBornAfter().before(addProductDto.getBornBefore())) {
                        return ResponseService.generateErrorResponse("BORN AFTER DATE MUST BE PAST OF BORN BEFORE DATE", HttpStatus.BAD_REQUEST);
                    }

                    productReserveCategoryBornBeforeAfterRefService.saveBornBeforeAndBornAfter(addProductDto.getBornBefore(), addProductDto.getBornAfter(), customProduct, reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory()));
                    productReserveCategoryFeePostRefService.saveFeeAndPost(addProductDto.getFee(), addProductDto.getPost(), customProduct, reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory()));

                }
            }

            return ResponseService.generateSuccessResponse("Product Updated Successfully", "UPDATED", HttpStatus.OK);

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
            return new ResponseEntity<>(Constant.NUMBER_FORMAT_EXCEPTION + ": " + numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
                return ResponseService.generateErrorResponse("NO PRODUCT FOUND", HttpStatus.NOT_FOUND);
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

            return ResponseService.generateSuccessResponse("PRODUCTS FOUND SUCCESSFULLY", responses, HttpStatus.OK);

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

            if (customProduct == null) {
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


}
