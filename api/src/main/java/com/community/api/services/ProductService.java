package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddProductDto;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.entity.*;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.ProductImpl;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.*;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.context.annotation.ApplicationScope;

import static com.community.api.component.Constant.PRODUCTTITLENOTGIVEN;
import static com.community.api.endpoint.avisoft.controller.product.ProductController.*;

@Service
public class ProductService {

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    ReserveCategoryDtoService reserveCategoryDtoService;

    @Autowired
    ProductStateService productStateService;

    @Autowired
    ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;

    @Autowired
    ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;

    @Autowired
    ReserveCategoryService reserveCategoryService;

    @Autowired
    RoleService roleService;

    @Autowired
    PrivilegeService privilegeService;

    @Autowired
    ApplicationScopeService applicationScopeService;

    @Autowired
    JwtUtil jwtTokenUtil;

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    JobGroupService jobGroupService;

    @PersistenceContext
    private EntityManager entityManager;

    @Resource(
            name = "blCatalogService"
    )
    protected CatalogService catalogService;

    public void saveCustomProduct(Product product, AddProductDto addProductDto, CustomProductState productState, Role role, Long creatorUserId, Date modifiedDate) {

        String sql = "INSERT INTO custom_product (product_id, exam_date_from, exam_date_to, go_live_date, platform_fee, priority_level, application_scope_id, job_group_id, product_state_id, creator_role_id, creator_user_id, notifying_authority, last_modified, advertiser_url, domicile_required) VALUES (:productId, :examDateFrom, :examDateTo, :goLiveDate, :platformFee, :priorityLevel, :applicationScopeId, :jobGroupId, :productStateId, :roleId, :userId, :notifyingAuthority, :modifiedDate, :advertiserUrl, :domicileRequired)";

        try {
            entityManager.createNativeQuery(sql)
                    .setParameter("productId", product)
                    .setParameter("examDateFrom", addProductDto.getExamDateFrom() != null ? new Timestamp(addProductDto.getExamDateFrom().getTime()) : null)
                    .setParameter("examDateTo", addProductDto.getExamDateTo() != null ? new Timestamp(addProductDto.getExamDateTo().getTime()) : null)
                    .setParameter("goLiveDate", addProductDto.getGoLiveDate() != null ? new Timestamp(addProductDto.getGoLiveDate().getTime()) : null)
                    .setParameter("platformFee", addProductDto.getPlatformFee())
                    .setParameter("priorityLevel", addProductDto.getPriorityLevel())
                    .setParameter("applicationScopeId", addProductDto.getApplicationScope())
                    .setParameter("jobGroupId", addProductDto.getJobGroup())
                    .setParameter("productStateId", productState.getProductStateId())
                    .setParameter("roleId", role.getRole_id())
                    .setParameter("userId", creatorUserId)
                    .setParameter("notifyingAuthority", addProductDto.getNotifyingAuthority())
                    .setParameter("modifiedDate",  modifiedDate)
                    .setParameter("advertiserUrl", addProductDto.getAdvertiserUrl())
                    .setParameter("domicileRequired", addProductDto.getDomicileRequired())

                    .executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to save Custom Product: " + e.getMessage(), e);
        }
    }

    public List<CustomProduct> getCustomProducts() {
        String sql = "SELECT * FROM custom_product";

        return entityManager.createNativeQuery(sql, CustomProduct.class).getResultList();
    }

    public CustomProduct getCustomProductByCustomProductId(Long productId) {
        String sql = "SELECT c FROM CustomProduct c WHERE c.id = :productId";
        return entityManager.createQuery(sql, CustomProduct.class).setParameter("productId", productId).getResultList().get(0);
    }

    @Transactional
    public void removeCategoryProductFromCategoryProductRefTable(Long categoryId, Long productId) {
        String sql = "DELETE FROM blc_category_product_xref WHERE product_id = :productId AND category_id = :categoryId";
        try {
            entityManager.createNativeQuery(sql)
                    .setParameter("productId", productId)
                    .setParameter("categoryId", categoryId)
                    .executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to Delete Category_Product: " + e.getMessage(), e);
        }
    }

    public Map<String, String> getRequestParamBasedOnQueryString(String queryString) throws UnsupportedEncodingException {
        if (queryString != null) {

            String[] params = queryString.split("&"); // Split the query string by '&' to get each parameter

            // Create a map to hold parameters
            Map<String, String> paramMap = new HashMap<>();

            // Process each parameter
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];

                    // Encode the value to UTF-8
                    value = URLEncoder.encode(value, "UTF-8"); // may throw exception.

                    paramMap.put(key, value);
                }
            }
            return paramMap;
        } else {
            return null;
        }
    }

    public ResponseEntity<?> validateAndSetActiveEndDateAndGoLiveDateFields(AddProductDto addProductDto, CustomProduct customProduct) {
        try {
            if (addProductDto.getActiveEndDate() != null) {
                Date activeEndDate = dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
                Date goLiveDate = addProductDto.getGoLiveDate() != null ? dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate())) : null;

                if (!activeEndDate.after(customProduct.getActiveStartDate())) {
                    return ResponseService.generateErrorResponse("ACTIVE END DATE CANNOT BE BEFORE OR EQUAL OF ACTIVE START DATE", HttpStatus.BAD_REQUEST);
                }
                if (goLiveDate != null && (!activeEndDate.after(goLiveDate) || !goLiveDate.after(customProduct.getActiveStartDate()))) {
                    return ResponseService.generateErrorResponse("GO LIVE DATE CANNOT BE BEFORE OR EQUAL OF GO LIVE DATE AND BEFORE OR EQUAL OF ACTIVE START DATE", HttpStatus.BAD_REQUEST);
                }
                if (addProductDto.getExamDateFrom() != null) {
                    Date examDateFrom = dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                    if (!activeEndDate.before(examDateFrom)) {
                        return ResponseService.generateErrorResponse("ACTIVE END DATE CANNOT BE AFTER OR EQUAL OF EXAM DATE FROM DATE", HttpStatus.BAD_REQUEST);
                    }
                } else if (!activeEndDate.before(customProduct.getExamDateFrom())) {
                    return ResponseService.generateErrorResponse("ACTIVE END DATE CANNOT BE AFTER OR EQUAL OF EXAM DATE FROM DATE", HttpStatus.BAD_REQUEST);
                }
                customProduct.getDefaultSku().setActiveEndDate(activeEndDate);
                customProduct.setGoLiveDate(goLiveDate);
            } else if (addProductDto.getGoLiveDate() != null) {
                Date goLiveDate = dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));
                if (!goLiveDate.after(customProduct.getActiveStartDate())) {
                    return ResponseService.generateErrorResponse("GO LIVE DATE CANNOT BE BEFORE OR EQUAL OF ACTIVE START DATE", HttpStatus.BAD_REQUEST);
                }
                if (!customProduct.getActiveEndDate().after(goLiveDate)) {
                    return ResponseService.generateErrorResponse("GO LIVE DATE CANNOT BE AFTER AND EQUAL OF EXPIRY DATE", HttpStatus.BAD_REQUEST);
                }
                customProduct.setGoLiveDate(goLiveDate);
                return ResponseService.generateSuccessResponse("GO LIVE DATE AND ACTIVE END DATE ARE VALIDATED PROPERLY", "VALIDATED", HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("GO LIVE DATE AND ACTIVE END DATE ARE VALIDATED PROPERLY", "EMPTY GO LIVE DATE AND ACTIVE END DATE", HttpStatus.OK);
        } catch (ParseException parseException) {
            return ResponseService.generateErrorResponse("DATE PARSE EXCEPTION: " + parseException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            return ResponseService.generateErrorResponse("SOME EXCEPTION OCCURRED: " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> validateAndSetExamDateFromAndExamDateToFields(AddProductDto addProductDto, CustomProduct customProduct) {
        try {
            if (addProductDto.getExamDateFrom() != null && addProductDto.getExamDateTo() != null) {

                // Validation on date for being wrong types. -> these needs to be changed or we have to add exception.
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

                if (addProductDto.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    return ResponseService.generateErrorResponse(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM, HttpStatus.BAD_REQUEST);
                }

                if (addProductDto.getActiveEndDate() != null) {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate()) || !addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        return ResponseService.generateErrorResponse(TENTATIVEDATEAFTERACTIVEENDDATE, HttpStatus.BAD_REQUEST);
                    }

                } else {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate()) || !addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        return ResponseService.generateErrorResponse(TENTATIVEDATEAFTERACTIVEENDDATE, HttpStatus.BAD_REQUEST);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());
                customProduct.setExamDateTo(addProductDto.getExamDateTo());
                return ResponseService.generateSuccessResponse("EXAM DATE FROM AND EXAM DATE TO ARE VALIDATED SUCCESSFULLY", "VALIDATED", HttpStatus.OK);

            } else if (addProductDto.getExamDateFrom() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                if (customProduct.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    return ResponseService.generateErrorResponse(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM, HttpStatus.BAD_REQUEST);
                }

                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate())) {
                        return ResponseService.generateErrorResponse(TENTATIVEEXAMDATEAFTERACTIVEENDDATE, HttpStatus.BAD_REQUEST);
                    }
                } else {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        return ResponseService.generateErrorResponse(TENTATIVEEXAMDATEAFTERACTIVEENDDATE, HttpStatus.BAD_REQUEST);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());
                return ResponseService.generateSuccessResponse("EXAM DATE FROM IS VALIDATED SUCCESSFULLY", "VALIDATED", HttpStatus.OK);

            } else if (addProductDto.getExamDateTo() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));
                if (addProductDto.getExamDateTo().before(customProduct.getExamDateFrom())) {
                    return ResponseService.generateErrorResponse(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM, HttpStatus.BAD_REQUEST);
                }
                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        return ResponseService.generateErrorResponse(TENTATIVEEXAMDATEAFTERACTIVEENDDATE, HttpStatus.BAD_REQUEST);
                    }
                } else {
                    if (!addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        return ResponseService.generateErrorResponse("TENTATIVE EXAMINATION DATA MUST BE AFTER ACTIVE END DATE", HttpStatus.BAD_REQUEST);
                    }
                }
                customProduct.setExamDateTo(addProductDto.getExamDateTo());
                return ResponseService.generateSuccessResponse("EXAM DATE TO IS VALIDATED SUCCESSFULLY", "VALIDATED", HttpStatus.OK);

            }
            return ResponseService.generateSuccessResponse("EXAM DATE FROM AND EXAM DATE TO ARE VALIDATED SUCCESSFULLY", "EMPTY EXAM DATE FROM AND EMPTY EXAM DATE TO", HttpStatus.OK);

        } catch (ParseException parseException) {
            return ResponseService.generateErrorResponse("DATE PARSE EXCEPTION: " + parseException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            return ResponseService.generateErrorResponse("SOME EXCEPTION OCCURRED: " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

/*    public ResponseEntity<?> validateAndSetReserveCategoryFields(AddProductDto addProductDto, CustomProduct customProduct) {
        try {
            if (addProductDto.getReservedCategory() != null) {
                List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(customProduct.getId());
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

                    CustomProductReserveCategoryBornBeforeAfterRef customProductReserveCategoryBornBeforeAfterRef = productReserveCategoryBornBeforeAfterRefService.getCustomProductReserveCategoryBornBeforeAfterRefByProductIdAndReserveCategoryId(customProduct.getId(), addProductDto.getReservedCategory());
                    CustomProductReserveCategoryFeePostRef customProductReserveCategoryFeePostRef = productReserveCategoryFeePostRefService.getCustomProductReserveCategoryFeePostRefByProductIdAndReserveCategoryId(customProduct.getId(), addProductDto.getReservedCategory());

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

                    if (addProductDto.getFee() != null) {
                        if (addProductDto.getFee() <= 0) {
                            return ResponseService.generateErrorResponse(FEELESSTHANOREQUALZERO, HttpStatus.BAD_REQUEST);
                        }
                        customProductReserveCategoryFeePostRef.setFee(addProductDto.getFee());
                    }
                    if (addProductDto.getPost() != null) {
                        if (addProductDto.getPost() <= 0) {
                            return ResponseService.generateErrorResponse(POSTLESSTHANORZERO, HttpStatus.BAD_REQUEST);
                        }
                        customProductReserveCategoryFeePostRef.setPost(addProductDto.getPost());
                    }
                    entityManager.persist(customProductReserveCategoryFeePostRef);
                    return ResponseService.generateSuccessResponse("RESERVED CATEGORY VALIDATED SUCCESSFULLY", "VALIDATED", HttpStatus.OK);
                } else {
                    if (addProductDto.getFee() == null || addProductDto.getBornAfter() == null || addProductDto.getBornBefore() == null) {
                        return ResponseService.generateErrorResponse("FEE, POST, BORN BEFORE AND BORN AFTER ARE MANDATORY FOR NEW RESERVE CATEGORY DATA", HttpStatus.BAD_REQUEST);
                    }
                    if (addProductDto.getFee() <= 0) {
                        return ResponseService.generateErrorResponse(FEELESSTHANOREQUALZERO, HttpStatus.BAD_REQUEST);
                    }
                    if (addProductDto.getPost() == null) {
                        addProductDto.setPost(Constant.DEFAULT_QUANTITY);
                    } else if (addProductDto.getPost() <= 0) {
                        return ResponseService.generateErrorResponse(POSTLESSTHANORZERO, HttpStatus.BAD_REQUEST);
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

                    return ResponseService.generateSuccessResponse("RESERVED CATEGORY VALIDATED SUCCESSFULLY", "VALIDATED", HttpStatus.OK);
                }
            } else {
                return ResponseService.generateSuccessResponse("RESERVE CATEGORY VALIDATED SUCCESSFULLY", "EMPTY RESERVE CATEGORY", HttpStatus.OK);
            }
        } catch (ParseException parseException) {
            return ResponseService.generateErrorResponse("DATE PARSE EXCEPTION: " + parseException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            return ResponseService.generateErrorResponse("SOME EXCEPTION OCCURRED: " + exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }*/

    public List<CustomProduct> filterProducts(List<Long> states, List<Long> categories, List<Long> reserveCategories, String title, Double fee, Integer post, Date startRange, Date endRange) {

        /*String jpql = "SELECT DISTINCT p FROM CustomProduct p "
                + "JOIN CustomProductReserveCategoryFeePostRef r "
                + "ON r.customProduct = p "
                + "WHERE " ;

        TypedQuery<CustomProduct> query = entityManager.createQuery(jpql, CustomProduct.class);

        List<CustomProductState> customProductStates = new ArrayList<>();
        if (states != null && !states.isEmpty()) {
            for (Long id : states) {
                customProductStates.add(productStateService.getProductStateById(id));
            }
            jpql += "p.productState IN :states ";
            query.setParameter("states", customProductStates);
        }
        List<Category> categoryList = new ArrayList<>();
        if (categories != null && !categories.isEmpty()) {
            for (Long id : categories) {
                categoryList.add(catalogService.findCategoryById(id));
            }
            jpql += "AND p.defaultCategory IN :categories ";
            query.setParameter("categories", categoryList);
        }
        List<CustomReserveCategory> customReserveCategoryList = new ArrayList<>();
        if (reserveCategories != null && !reserveCategories.isEmpty()) {
            for (Long id : reserveCategories) {
                customReserveCategoryList.add(reserveCategoryService.getReserveCategoryById(id));
            }
            jpql += "AND r.customReserveCategory IN :reserveCategories";
            query.setParameter("reserveCategories", customReserveCategoryList);
        }

        if(title != null) {
            jpql += "AND p.metaTitle LIKE :title ";
            query.setParameter("title", "%" + title + "%");
        }

        if(fee != null) {
            jpql += "AND r.fee > :fee ";
            query.setParameter("fee", fee);
        }

        if(post != null) {
            jpql += "AND r.post > :post ";
            query.setParameter("post", post);
        }

        return query.getResultList();
*/
        // Initialize the JPQL query
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT p FROM CustomProduct p ")
                .append("JOIN CustomProductReserveCategoryFeePostRef r ON r.customProduct = p ")
                .append("WHERE 1=1 "); // Use this to simplify appending conditions

        // List to hold query parameters
        List<CustomProductState> customProductStates = new ArrayList<>();
        List<Category> categoryList = new ArrayList<>();
        List<CustomReserveCategory> customReserveCategoryList = new ArrayList<>();

        // Conditionally build the query
        if (states != null && !states.isEmpty()) {
            for (Long id : states) {
                customProductStates.add(productStateService.getProductStateById(id));
            }
            jpql.append("AND p.productState IN :states ");
        }

        if (categories != null && !categories.isEmpty()) {
            for (Long id : categories) {
                categoryList.add(catalogService.findCategoryById(id));
            }
            jpql.append("AND p.defaultCategory IN :categories ");
        }

        if (reserveCategories != null && !reserveCategories.isEmpty()) {
            for (Long id : reserveCategories) {
                customReserveCategoryList.add(reserveCategoryService.getReserveCategoryById(id));
            }
            jpql.append("AND r.customReserveCategory IN :reserveCategories ");
        }

        if (title != null && !title.isEmpty()) {
            jpql.append("AND p.metaTitle LIKE :title ");
        }

        if (fee != null) {
            jpql.append("AND r.fee > :fee ");
        }

        if (post != null) {
            jpql.append("AND r.post > :post ");
        }





        // Create the query with the final JPQL string
        TypedQuery<CustomProduct> query = entityManager.createQuery(jpql.toString(), CustomProduct.class);

        // Set parameters
        if (!customProductStates.isEmpty()) {
            query.setParameter("states", customProductStates);
        }
        if (!categoryList.isEmpty()) {
            query.setParameter("categories", categoryList);
        }
        if (!customReserveCategoryList.isEmpty()) {
            query.setParameter("reserveCategories", customReserveCategoryList);
        }
        if (title != null && !title.isEmpty()) {
            query.setParameter("title", "%" + title + "%");
        }
        if (fee != null) {
            query.setParameter("fee", fee);
        }
        if (post != null) {
            query.setParameter("post", post);
        }

        // Execute and return the result
        return query.getResultList();
    }
    public boolean addProductAccessAuthorisation(String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();

            Long userId = null;
            if (role.equals(Constant.SUPER_ADMIN) || role.equals(Constant.ADMIN)) {
                return true;

                // -> NEED TO ADD THE USER_ID OF ADMIN OR SUPER ADMIN.

            } else if (role.equals(Constant.SERVICE_PROVIDER)) {
                userId = jwtTokenUtil.extractId(jwtToken);
                List<Privileges> privileges = privilegeService.getServiceProviderPrivilege(userId);

                for (Privileges privilege : privileges) {
                    if (privilege.getPrivilege_name().equals(Constant.PRIVILEGE_ADD_PRODUCT)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }
    public Category validateCategory(Long categoryId) throws Exception {
        try {
            if (categoryId <= 0) throw new IllegalArgumentException("CATEGORY ID CANNOT BE <= 0");
            Category category = catalogService.findCategoryById(categoryId);
            return category;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATING CATEGORY: " + exception.getMessage() + "\n");
        }
    }
    public boolean addProductDtoValidation(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getQuantity() != null) {
                if (addProductDto.getQuantity() <= 0) {
                    throw new IllegalArgumentException("QUANTITY CANNOT BE EMPTY <= 0");
                }
            } else {
                addProductDto.setQuantity(Constant.DEFAULT_QUANTITY);
            }

            if (addProductDto.getPriorityLevel() == null) {
                addProductDto.setPriorityLevel(Constant.DEFAULT_PRIORITY_LEVEL);
            }

            if (addProductDto.getMetaTitle() == null || addProductDto.getMetaTitle().trim().isEmpty()) {
                throw new IllegalArgumentException(PRODUCTTITLENOTGIVEN);
            } else {
                addProductDto.setMetaTitle(addProductDto.getMetaTitle().trim());
            }

            if (addProductDto.getDisplayTemplate() != null && !addProductDto.getDisplayTemplate().trim().isEmpty()) {
                addProductDto.setDisplayTemplate(addProductDto.getDisplayTemplate().trim());
            } else {
                addProductDto.setDisplayTemplate(addProductDto.getMetaTitle());
            }

            if (addProductDto.getMetaDescription() != null && !addProductDto.getMetaDescription().trim().isEmpty()) {
                addProductDto.setMetaDescription(addProductDto.getMetaDescription().trim());
            } else {
                throw new IllegalArgumentException("DESCRIPTION CANNOT BE NULL OR EMPTY");
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date activeStartDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date

            if (addProductDto.getActiveEndDate() == null || addProductDto.getGoLiveDate() == null) {
                throw new IllegalArgumentException("ACTIVE END DATE AND GO LIVE DATE CANNOT BE EMPTY");
            }

            dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
            dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));
            dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
            dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

            if (!addProductDto.getActiveEndDate().after(activeStartDate)) {
                throw new IllegalArgumentException("EXPIRATION DATE CANNOT BE BEFORE OR EQUAL OF CURRENT DATE");
            } else if (!addProductDto.getActiveEndDate().after(addProductDto.getGoLiveDate()) || !addProductDto.getGoLiveDate().after(activeStartDate)) {
                throw new IllegalArgumentException("GO LIVE DATE CANNOT BE AFTER OR EQUAL OF ACTIVE END DATE AND BEFORE OR EQUAL OF CURRENT DATE");
            }

            if (addProductDto.getExamDateFrom() == null || addProductDto.getExamDateTo() == null) {
                throw new IllegalArgumentException("TENTATIVE EXAMINATION DATE FROM-TO CANNOT BE NULL");
            }

            if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate()) || !addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException(TENTATIVEDATEAFTERACTIVEENDDATE);
            } else if (addProductDto.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
            }

            if(addProductDto.getJobGroup() <= 0) {
                throw new IllegalArgumentException("JOB GROUP CANNOT BE <= 0");
            }

            if (addProductDto.getAdvertiserUrl() == null || addProductDto.getAdvertiserUrl().trim().isEmpty()) {
                throw new IllegalArgumentException("ADVERTISER URL CANNOT BE NULL OR EMPTY");
            }
            addProductDto.setAdvertiserUrl(addProductDto.getAdvertiserUrl().trim());

            if(addProductDto.getApplicationScope() == null || addProductDto.getApplicationScope() <= 0) {
                throw new IllegalArgumentException("APPLICATION SCOPE CANNOT BE NULL OR <= 0");
            }

            CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());
            if(applicationScope == null) {
                throw new NoSuchElementException("APPLICATION SCOPE NOT FOUND");
            }

            if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_CENTER)) {
                addProductDto.setDomicileRequired(false);
            } else {
                if (addProductDto.getDomicileRequired() == null) {
                    throw new IllegalArgumentException("APPLICATION SCOPE IS: " + applicationScope.getApplicationScope() + " DOMICILE CANNOT BE NULL.");
                }
            }

            if(applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_STATE)) {
                if (addProductDto.getNotifyingAuthority() == null || addProductDto.getNotifyingAuthority().trim().isEmpty()) {
                    throw new IllegalArgumentException("NOTIFYING AUTHORITY CANNOT BE NULL/EMPTY IF APPLICATION SCOPE IS STATE");
                }
                addProductDto.setNotifyingAuthority(addProductDto.getNotifyingAuthority().trim());
            }else {
                if (addProductDto.getNotifyingAuthority() != null) {
                    throw new IllegalArgumentException("NOTIFYING AUTHORITY CANNOT BE GIVEN IF APPLICATION SCOPE IS CENTER");
                }
            }

            if(addProductDto.getReservedCategory().isEmpty()){
                throw new IllegalArgumentException("RESERVE CATEGORY MUST NOT BE EMPTY");
            }

            return true;
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("PARSE EXCEPTION CAUGHT WHILE VALIDATING ADD PRODUCT DTO: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATING ADD PRODUCT DTO: " + exception.getMessage() + "\n");
        }

    }
    public CustomJobGroup validateCustomJobGroup(Long customJobGroupId) throws Exception {
        try{
            CustomJobGroup jobGroup = jobGroupService.getJobGroupById(customJobGroupId);
            return jobGroup;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION WHILE VALIDATING ADD PRODUCT DTO: " + exception.getMessage() + "\n");
        }
    }
    public Role getRoleByToken(String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Role role = roleService.getRoleByRoleId(roleId);
            return role;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }
    public Long getUserIdByToken(String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);
            Long userId = jwtTokenUtil.extractId(jwtToken);

            return userId;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }
    public boolean validateReserveCategory(AddProductDto addProductDto) throws Exception {
        try{
            Set<Long> reserveCategoryId = new HashSet<>();
            for(int reserveCategoryIndex=0; reserveCategoryIndex<addProductDto.getReservedCategory().size(); reserveCategoryIndex++){
                if(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory() == null ||  addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory() <= 0){
                    throw new IllegalArgumentException("RESERVE CATEGORY ID CANNOT BE NULL OR <= 0");
                }
                reserveCategoryId.add(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());

                CustomReserveCategory reserveCategory = reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());
                if(reserveCategory == null) {
                    throw new IllegalArgumentException("RESERVE CATEGORY NOT FOUND WITH ID: " + addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());
                }

                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getFee() == null || addProductDto.getReservedCategory().get(reserveCategoryIndex).getFee() <= 0) {
                    throw new IllegalArgumentException("FEE CANNOT BE NULL OR <= 0");
                }

                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getPost() == null) {
                    addProductDto.getReservedCategory().get(reserveCategoryIndex).setPost(Constant.DEFAULT_QUANTITY);
                } else if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getPost() <= 0) {
                    throw new IllegalArgumentException(POSTLESSTHANORZERO);
                }

                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore() == null || addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter() == null) {
                    throw new IllegalArgumentException("BORN BEFORE DATE AND BORN AFTER DATE CANNOT BE EMPTY");
                }

                // Validation on date for being wrong types. -> these needs to be changed or we have to add exception.
                dateFormat.parse(dateFormat.format(addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter()));
                dateFormat.parse(dateFormat.format(addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore()));

                if (!addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore().before(new Date()) || !addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter().before(new Date())) {
                    throw new IllegalArgumentException("BORN BEFORE DATE AND BORN AFTER DATE MUST BE OF PAST");
                } else if (!addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter().before(addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore())) {
                    throw new IllegalArgumentException("BORN AFTER DATE MUST BE PAST OF BORN BEFORE DATE");
                }
            }

            if(reserveCategoryId.size() != addProductDto.getReservedCategory().size()) {
                throw new IllegalArgumentException("DUPLICATE RESERVE CATEGORIES NOT ALLOWED");
            }

            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }

    public boolean updateProductAccessAuthorisation(String authHeader, Long productId) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();

            if (productId <= 0) {
                throw new IllegalArgumentException("PRODUCT ID CANNOT BE <= 0");
            }
            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);
            if (customProduct == null || ((Status) customProduct).getArchived() == 'Y') {
                throw new IllegalArgumentException(PRODUCTNOTFOUND);
            }

            Long userId = null;
            if (role.equals(Constant.SUPER_ADMIN) || role.equals(Constant.ADMIN)) {
                return true;

                // -> NEED TO ADD THE USER_ID OF ADMIN OR SUPER ADMIN.

            } else if (role.equals(Constant.SERVICE_PROVIDER)) {

                userId = jwtTokenUtil.extractId(jwtToken);
                List<Privileges> privileges = privilegeService.getServiceProviderPrivilege(userId);
                for (Privileges privilege : privileges) {
                    if (privilege.getPrivilege_name().equals(Constant.PRIVILEGE_UPDATE_PRODUCT)) {
                        return true;
                    }
                }

            }

            return false;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }
}