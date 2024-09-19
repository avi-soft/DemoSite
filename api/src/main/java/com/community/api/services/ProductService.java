package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddProductDto;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomJobGroup;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductState;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.entity.Privileges;
import com.community.api.entity.Role;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.community.api.component.Constant.*;
import static com.community.api.component.Constant.PRODUCTNOTFOUND;
import static com.community.api.endpoint.avisoft.controller.product.ProductController.*;

@Service
public class ProductService {

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Resource(
            name = "blCatalogService"
    )
    protected CatalogService catalogService;
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
                    .setParameter("modifiedDate", modifiedDate)
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
                .append("JOIN SkuImpl s ON s.defaultProduct = p ")
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

        if (startRange != null && endRange != null) {
            jpql.append("AND s.activeStartDate BETWEEN :startRange AND :endRange ");
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
        if (startRange != null && endRange != null) {
            query.setParameter("startRange", startRange);
            query.setParameter("endRange", endRange);
        }

        System.out.println(startRange);
        System.out.println(endRange);
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
            } else if (addProductDto.getPriorityLevel() <= 0 || addProductDto.getPriorityLevel() > 5) {
                throw new IllegalArgumentException("PRIORITY LEVEL MUST LIE BETWEEN 1-5");
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

            if (addProductDto.getJobGroup() == null || addProductDto.getJobGroup() <= 0) {
                throw new IllegalArgumentException("JOB GROUP CANNOT BE NULL OR <= 0");
            }

            CustomJobGroup jobGroup = jobGroupService.getJobGroupById(addProductDto.getJobGroup());
            if (jobGroup == null) {
                throw new NoSuchElementException("JOB GROUP NOT FOUND");
            }

            if (addProductDto.getAdvertiserUrl() == null || addProductDto.getAdvertiserUrl().trim().isEmpty()) {
                throw new IllegalArgumentException("ADVERTISER URL CANNOT BE NULL OR EMPTY");
            }
            addProductDto.setAdvertiserUrl(addProductDto.getAdvertiserUrl().trim());

            if (addProductDto.getApplicationScope() == null || addProductDto.getApplicationScope() <= 0) {
                throw new IllegalArgumentException("APPLICATION SCOPE CANNOT BE NULL OR <= 0");
            }

            CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());
            if (applicationScope == null) {
                throw new NoSuchElementException("APPLICATION SCOPE NOT FOUND");
            }

            if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_CENTER)) {
                addProductDto.setDomicileRequired(false);
            } else {
                if (addProductDto.getDomicileRequired() == null) {
                    throw new IllegalArgumentException("APPLICATION SCOPE IS: " + applicationScope.getApplicationScope() + " DOMICILE CANNOT BE NULL.");
                }
            }

            if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_STATE)) {
                if (addProductDto.getNotifyingAuthority() == null || addProductDto.getNotifyingAuthority().trim().isEmpty()) {
                    throw new IllegalArgumentException("NOTIFYING AUTHORITY CANNOT BE NULL/EMPTY IF APPLICATION SCOPE IS STATE");
                }
                addProductDto.setNotifyingAuthority(addProductDto.getNotifyingAuthority().trim());
            } else {
                if (addProductDto.getNotifyingAuthority() != null) {
                    throw new IllegalArgumentException("NOTIFYING AUTHORITY CANNOT BE GIVEN IF APPLICATION SCOPE IS CENTER");
                }
            }

            if (addProductDto.getReservedCategory().isEmpty()) {
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
        try {
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
        try {

            if (addProductDto.getReservedCategory().isEmpty()) {
                throw new IllegalArgumentException("RESERVE CATEGORY CANNOT BE EMPTY");
            }
            Set<Long> reserveCategoryId = new HashSet<>();

            Date currentDate = new Date(); // Current date for comparison
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);

            calendar.add(Calendar.YEAR, -105);
            Date minBornAfterDate = calendar.getTime();
            calendar.add(Calendar.YEAR, 100);
            Date maxBornBeforeDate = calendar.getTime();

            for (int reserveCategoryIndex = 0; reserveCategoryIndex < addProductDto.getReservedCategory().size(); reserveCategoryIndex++) {
                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory() == null || addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory() <= 0) {
                    throw new IllegalArgumentException("RESERVE CATEGORY ID CANNOT BE NULL OR <= 0");
                }
                reserveCategoryId.add(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());

                CustomReserveCategory reserveCategory = reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());
                if (reserveCategory == null) {
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

                // Ensure dates are within the allowed range
                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter().before(minBornAfterDate)) {
                    throw new IllegalArgumentException("BORN AFTER DATE CANNOT BE MORE THAN 105 YEARS IN THE PAST");
                }
                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore().after(maxBornBeforeDate)) {
                    throw new IllegalArgumentException("BORN BEFORE DATE MUST BE AT LEAST 5 YEARS IN THE PAST");
                }
            }

            if (reserveCategoryId.size() != addProductDto.getReservedCategory().size()) {
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
                if (customProduct.getCreatoRole().getRole_name().equals(role) && customProduct.getUserId().equals(userId)) {
                    return true;
                }

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

    public boolean updateProductValidation(AddProductDto addProductDto, CustomProduct customProduct) throws Exception {
        try {
            if (addProductDto.getQuantity() != null) {
                if (addProductDto.getQuantity() <= 0) {
                    throw new IllegalArgumentException("QUANTITY CANNOT BE EMPTY <= 0");
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
            }

            if (addProductDto.getDisplayTemplate() != null && !addProductDto.getDisplayTemplate().trim().isEmpty()) {
                customProduct.setDisplayTemplate(addProductDto.getDisplayTemplate().trim());
            }

            if ((addProductDto.getPriorityLevel() != null) && (addProductDto.getPriorityLevel() <= 0 || addProductDto.getPriorityLevel() > 5)) {
                throw new IllegalArgumentException("PRIORITY LEVEL MUST LIE BETWEEN 1-5");
            }
            if (addProductDto.getMetaDescription() != null && !addProductDto.getMetaDescription().trim().isEmpty()) {
                addProductDto.setMetaDescription(addProductDto.getMetaDescription().trim());
                customProduct.setMetaDescription(addProductDto.getMetaDescription());
                customProduct.getDefaultSku().setDescription(addProductDto.getMetaDescription());
            }

            CustomJobGroup jobGroup;
            if (addProductDto.getJobGroup() != null) {

                jobGroup = jobGroupService.getJobGroupById(addProductDto.getJobGroup());
                if (jobGroup == null) {
                    throw new IllegalArgumentException("NO JOB GROUP EXISTS WITH THIS JOB GROUP ID");
                }
                customProduct.setJobGroup(jobGroup);
            }

            if (addProductDto.getPlatformFee() != null) {
                if (addProductDto.getPlatformFee() <= 0) {
                    throw new IllegalArgumentException("PLATFORM FEE CANNOT BE LESS THAN OR EQUAL TO ZERO");
                }
                customProduct.setPlatformFee(addProductDto.getPlatformFee());
            }

            if (addProductDto.getApplicationScope() != null) {
                CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());
                if (applicationScope == null) {
                    throw new IllegalArgumentException("NO APPLICATION SCOPE EXISTS WITH THIS ID");
                } else if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_STATE)) {
                    if (addProductDto.getNotifyingAuthority() == null || addProductDto.getDomicileRequired() == null) {
                        throw new IllegalArgumentException("NOTIFYING AUTHORITY AND DOMICILE REQUIRED CANNOT BE NULL IF APPLICATION SCOPE IS: " + Constant.APPLICATION_SCOPE_STATE);
                    } else if (!addProductDto.getNotifyingAuthority().trim().isEmpty()) {
                        addProductDto.setNotifyingAuthority(addProductDto.getNotifyingAuthority().trim());
                        customProduct.setNotifyingAuthority(addProductDto.getNotifyingAuthority());
                        customProduct.setCustomApplicationScope(applicationScope);
                    }
                } else if (applicationScope.getApplicationScope().equals(APPLICATION_SCOPE_CENTER)) {
                    if (addProductDto.getNotifyingAuthority() != null) {
                        throw new IllegalArgumentException("NOTIFYING AUTHORITY NOT REQUIRED IN CASE OF CENTER LEVEL APPLICATION SCOPE");
                    }
                    if (addProductDto.getDomicileRequired() != null && addProductDto.getDomicileRequired()) {
                        throw new IllegalArgumentException("DOMICILE IS NOT REQUIRED IN CASE OF CENTER APPLICATION SCOPE");
                    }

                    addProductDto.setDomicileRequired(false);
                    addProductDto.setNotifyingAuthority(null);
                    customProduct.setNotifyingAuthority(addProductDto.getNotifyingAuthority());
                    customProduct.setDomicileRequired(addProductDto.getDomicileRequired());
                    customProduct.setCustomApplicationScope(applicationScope);
                }
            }

            if (addProductDto.getAdvertiserUrl() != null && !addProductDto.getAdvertiserUrl().trim().isEmpty()) {
                addProductDto.setAdvertiserUrl(addProductDto.getAdvertiserUrl().trim());
                customProduct.setAdvertiserUrl(addProductDto.getAdvertiserUrl());
            }

            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }

    public Boolean validateAndSetActiveEndDateAndGoLiveDateFields(AddProductDto addProductDto, CustomProduct customProduct) throws Exception {
        try {
            if (addProductDto.getActiveEndDate() != null && addProductDto.getGoLiveDate() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
                dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));

                if (!addProductDto.getActiveEndDate().after(customProduct.getActiveStartDate())) {
                    throw new IllegalArgumentException("ACTIVE END DATE CANNOT BE BEFORE OR EQUAL OF ACTIVE START DATE");
                } else if (!addProductDto.getActiveEndDate().after(addProductDto.getGoLiveDate()) || !addProductDto.getGoLiveDate().after(customProduct.getActiveStartDate())) {
                    throw new IllegalArgumentException("GO LIVE DATE CANNOT BE BEFORE OR EQUAL OF GO LIVE DATE AND BEFORE OR EQUAL OF ACTIVE START DATE");
                } else if (addProductDto.getExamDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));

                    if (!addProductDto.getActiveEndDate().before(addProductDto.getExamDateFrom())) {
                        throw new IllegalArgumentException("ACTIVE END DATE CANNOT BE AFTER OR EQUAL OF EXAM DATE FROM DATE");
                    }
                } else {
                    if (!addProductDto.getActiveEndDate().before(customProduct.getExamDateFrom())) {
                        throw new IllegalArgumentException("ACTIVE END DATE CANNOT BE AFTER OR EQUAL OF EXAM DATE FROM DATE");
                    }
                }
                customProduct.getDefaultSku().setActiveEndDate(addProductDto.getActiveEndDate());
                customProduct.setGoLiveDate(addProductDto.getGoLiveDate());

            } else if (addProductDto.getActiveEndDate() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));

                if (!addProductDto.getActiveEndDate().after(customProduct.getActiveStartDate())) {
                    throw new IllegalArgumentException("ACTIVE END DATE CANNOT BE BEFORE OR EQUAL OF ACTIVE START DATE");
                } else if (!addProductDto.getActiveEndDate().after(customProduct.getGoLiveDate())) {
                    throw new IllegalArgumentException("ACTIVE END DATE CANNOT BE BEFORE OR EQUAL OF GO LIVE DATE");
                } else if (addProductDto.getExamDateFrom() != null) {

                    dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("EXAM DATE FROM MUST BE AFTER ACTIVE END DATE");
                    }
                } else {
                    if (!customProduct.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("EXAM DATE FROM MUST BE AFTER ACTIVE END DATE");
                    }
                }

                customProduct.getDefaultSku().setActiveEndDate(addProductDto.getActiveEndDate());
            } else if (addProductDto.getGoLiveDate() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));

                if (!addProductDto.getGoLiveDate().after(customProduct.getActiveStartDate())) {
                    throw new IllegalArgumentException("GO LIVE DATE CANNOT BE BEFORE OR EQUAL OF ACTIVE START DATE");
                } else if (!customProduct.getActiveEndDate().after(addProductDto.getGoLiveDate())) {
                    throw new IllegalArgumentException("GO LIVE DATE CANNOT BE AFTER AND EQUAL OF EXPIRY DATE");
                }
                customProduct.setGoLiveDate(addProductDto.getGoLiveDate());
            }

            return true;
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("PARSE EXCEPTION CAUGHT WHILE VALIDATING ADD PRODUCT DTO: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public Boolean validateAndSetExamDateFromAndExamDateToFields(AddProductDto addProductDto, CustomProduct customProduct) throws Exception {
        try {
            if (addProductDto.getExamDateFrom() != null && addProductDto.getExamDateTo() != null) {

                // Validation on date for being wrong types. -> these needs to be changed or we have to add exception.
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

                if (addProductDto.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }

                if (addProductDto.getActiveEndDate() != null) {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate()) || !addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEDATEAFTERACTIVEENDDATE);
                    }

                } else {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate()) || !addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEDATEAFTERACTIVEENDDATE);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());
                customProduct.setExamDateTo(addProductDto.getExamDateTo());

            } else if (addProductDto.getExamDateFrom() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                if (customProduct.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }

                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                } else {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());

            } else if (addProductDto.getExamDateTo() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));
                if (addProductDto.getExamDateTo().before(customProduct.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }
                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                } else {
                    if (!addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("TENTATIVE EXAMINATION DATA MUST BE AFTER ACTIVE END DATE");
                    }
                }
                customProduct.setExamDateTo(addProductDto.getExamDateTo());
            }
            return true;

        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("PARSE EXCEPTION CAUGHT WHILE VALIDATING ADD PRODUCT DTO: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean validateExamDateFromAndExamDateTo(AddProductDto addProductDto, CustomProduct customProduct) throws Exception {
        try {
            if (addProductDto.getExamDateFrom() != null && addProductDto.getExamDateTo() != null) {

                // Validation on date for being wrong types. -> these needs to be changed or we have to add exception.
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

                if (addProductDto.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }

                if (addProductDto.getActiveEndDate() != null) {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate()) || !addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEDATEAFTERACTIVEENDDATE);
                    }

                } else {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate()) || !addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEDATEAFTERACTIVEENDDATE);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());
                customProduct.setExamDateTo(addProductDto.getExamDateTo());

            } else if (addProductDto.getExamDateFrom() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                if (customProduct.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }

                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                } else {
                    if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());

            } else if (addProductDto.getExamDateTo() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));
                if (addProductDto.getExamDateTo().before(customProduct.getExamDateFrom())) {
                    throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
                }
                if (addProductDto.getActiveEndDate() == null) {
                    if (!addProductDto.getExamDateTo().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException(TENTATIVEEXAMDATEAFTERACTIVEENDDATE);
                    }
                } else {
                    if (!addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("TENTATIVE EXAMINATION DATA MUST BE AFTER ACTIVE END DATE");
                    }
                }
                customProduct.setExamDateTo(addProductDto.getExamDateTo());

            }
            return true;
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("PARSE EXCEPTION CAUGHT WHILE VALIDATING ADD PRODUCT DTO: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean validateProductState(AddProductDto addProductDto, CustomProduct customProduct, String authHeader) throws Exception {
        try {
            if (addProductDto.getProductState() != null) {

                String jwtToken = authHeader.substring(7);
                Long userId = jwtTokenUtil.extractId(jwtToken);

                Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
                String role = roleService.findRoleName(roleId);

                if (customProduct.getUserId().equals(userId)) {
                    throw new IllegalArgumentException("SERVICE PROVIDER WHO CREATED THE PRODUCT CANNOT CHANGE ITS STATE");
                }

                CustomProductState customProductState = productStateService.getProductStateById(addProductDto.getProductState());
                if (customProductState == null) {
                    throw new IllegalArgumentException("NO PRODUCT STATE EXIST WITH THIS ID");
                }

                if ( ( !customProduct.getProductState().getProductState().equals(Constant.PRODUCT_STATE_NEW) && !customProduct.getProductState().getProductState().equals(Constant.PRODUCT_STATE_MODIFIED) ) || ( !customProductState.getProductState().equals(PRODUCT_STATE_APPROVED) && !customProductState.getProductState().equals(PRODUCT_STATE_REJECTED)) ) {
                    throw new IllegalArgumentException("PRODUCT STATE ONLY CHANGE FROM NEW/MODIFIABLE TO APPROVED OR REJECTED STATE");
                }

                if (role.equals(Constant.SERVICE_PROVIDER)) {
                    List<Privileges> privileges = privilegeService.getServiceProviderPrivilege(userId);
                    for (Privileges privilege : privileges) {
                        if ((privilege.getPrivilege_name().equals(Constant.PRIVILEGE_APPROVE_PRODUCT) && customProduct.getProductState().getProductState().equals(Constant.PRODUCT_STATE_APPROVED)) || (privilege.getPrivilege_name().equals(Constant.PRIVILEGE_REJECT_PRODUCT) && customProduct.getProductState().getProductState().equals(Constant.PRODUCT_STATE_REJECTED))) {
                            customProduct.setProductState(customProductState);
                            break;
                        }
                    }
                } else if (role.equals(Constant.ADMIN) || role.equals(Constant.SUPER_ADMIN)) {
                    customProduct.setProductState(customProductState);
                }

                /*{
                    throw new IllegalArgumentException("PRODUCT STATE IS NOT MODIFIABLE");
                }*/

                customProduct.setProductState(customProductState);
            }
            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean deleteOldReserveCategoryMapping(CustomProduct customProduct) throws Exception {
        try {
            productReserveCategoryFeePostRefService.removeProductReserveCategoryFeeAndPostByProductId(customProduct);
            productReserveCategoryBornBeforeAfterRefService.removeProductReserveCategoryBornBeforeAfterByProductId(customProduct);
            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }
}