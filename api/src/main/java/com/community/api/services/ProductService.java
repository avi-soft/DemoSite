package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddProductDto;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomGender;
import com.community.api.entity.CustomJobGroup;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductRejectionStatus;
import com.community.api.entity.CustomProductState;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.entity.CustomSector;
import com.community.api.entity.CustomStream;
import com.community.api.entity.CustomSubject;
import com.community.api.entity.Privileges;
import com.community.api.entity.Qualification;
import com.community.api.entity.Role;
import com.community.api.entity.StateCode;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    @Resource(name = "blCatalogService")
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
    @Autowired
    ProductRejectionStatusService productRejectionStatusService;
    @Autowired
    DistrictService districtService;
    @Autowired
    GenderService genderService;
    @Autowired
    SectorService sectorService;
    @Autowired
    QualificationService qualificationService;
    @Autowired
    StreamService streamService;
    @Autowired
    SubjectService subjectService;
    @Autowired
    ProductGenderPhysicalRequirementService productGenderPhysicalRequirementService;
    @PersistenceContext
    private EntityManager entityManager;

    public void saveCustomProduct(Product product, AddProductDto addProductDto, CustomProductState productState, Role role, Long creatorUserId, Date modifiedDate, Date currentDate) {

        try {

            // Start building the SQL query
            StringBuilder sql = new StringBuilder("INSERT INTO custom_product (product_id, creator_user_id, creator_role_id, last_modified, product_state_id, created_date");
            StringBuilder values = new StringBuilder("VALUES (:productId, :creatorUserId, :role, :lastModified, :productState, :currentDate");

            // Dynamically add columns and values based on non-null fields
            if(addProductDto.getPostName() != null) {
                sql.append(", post_name");
                values.append(", :postName");
            }

            if(addProductDto.getApplicationScope() != null) {
                sql.append(", application_scope_id");
                values.append(", :applicationScope");
            }

            if (addProductDto.getExamDateFrom() != null) {
                sql.append(", exam_date_from");
                values.append(", :examDateFrom");
            }

            if (addProductDto.getAdvertiserUrl() != null) {
                sql.append(", advertiser_url");
                values.append(", :advertiserUrl");
            }

            if (addProductDto.getExamDateTo() != null) {
                sql.append(", exam_date_to");
                values.append(", :examDateTo");
            }

            if (addProductDto.getGoLiveDate() != null) {
                sql.append(", go_live_date");
                values.append(", :goLiveDate");
            }

            if (addProductDto.getPlatformFee() != null) {
                sql.append(", platform_fee");
                values.append(", :platformFee");
            }

            if (addProductDto.getPriorityLevel() != null) {
                sql.append(", priority_level");
                values.append(", :priorityLevel");
            }

            if (addProductDto.getAdmitCardDateFrom() != null) {
                sql.append(", admit_card_date_from");
                values.append(", :admitCardDateFrom");
            }

            if (addProductDto.getAdmitCardDateTo() != null) {
                sql.append(", admit_card_date_to");
                values.append(", :admitCardDateTo");
            }

            if (addProductDto.getModificationDateFrom() != null) {
                sql.append(", modification_date_from");
                values.append(", :modificationDateFrom");
            }

            if (addProductDto.getModificationDateTo() != null) {
                sql.append(", modification_date_to");
                values.append(", :modificationDateTo");
            }

            if (addProductDto.getState() != null) {
                sql.append(", state_id");
                values.append(", :state");
            }

            if (addProductDto.getLastDateToPayFee() != null) {
                sql.append(", last_date_to_pay_fee");
                values.append(", :lastDateToPayFee");
            }

            if (addProductDto.getDownloadNotificationLink() != null) {
                sql.append(", download_notification_link");
                values.append(", :downloadNotificationLink");
            }

            if (addProductDto.getDownloadSyllabusLink() != null) {
                sql.append(", download_syllabus_link");
                values.append(", :downloadSyllabusLink");
            }

            if (addProductDto.getFormComplexity() != null) {
                sql.append(", form_complexity");
                values.append(", :formComplexity");
            }

            if (addProductDto.getGenderSpecific() != null) {
                sql.append(", gender_specific_id");
                values.append(", :genderSpecificId");
            }

            if (addProductDto.getSector() != null) {
                sql.append(", sector_id");
                values.append(", :sectorId");
            }

            if (addProductDto.getSelectionCriteria() != null) {
                sql.append(", selection_criteria");
                values.append(", :selectionCriteria");
            }

            if (addProductDto.getQualification() != null) {
                sql.append(", qualification_id");
                values.append(", :qualificationId");
            }

            if (addProductDto.getStream() != null) {
                sql.append(", stream_id");
                values.append(", :streamId");
            }

            if (addProductDto.getSubject() != null) {
                sql.append(", subject_id");
                values.append(", :subjectId");
            }

            if (addProductDto.getJobGroup() != null) {
                sql.append(", job_group_id");
                values.append(", :jobGroup");
            }

            // Complete the SQL statement
            sql.append(") ").append(values).append(")");

            // Create the query
            var query = entityManager.createNativeQuery(sql.toString())
                    .setParameter("productId", product)
                    .setParameter("creatorUserId", creatorUserId)
                    .setParameter("role", role)
                    .setParameter("lastModified", modifiedDate)
                    .setParameter("currentDate", currentDate);

            // Set parameters conditionally
            if(addProductDto.getPostName() != null) {
                query.setParameter("postName", addProductDto.getPostName());
            }

            if(addProductDto.getApplicationScope() != null) {
                query.setParameter("applicationScope", addProductDto.getApplicationScope());
            }

            if (addProductDto.getExamDateFrom() != null) {
                query.setParameter("examDateFrom", new Timestamp(addProductDto.getExamDateFrom().getTime()));
            }

            if (addProductDto.getAdvertiserUrl() != null) {
                query.setParameter("advertiserUrl", addProductDto.getAdvertiserUrl());
            }

            if (addProductDto.getJobGroup() != null) {
                query.setParameter("jobGroup", addProductDto.getJobGroup());
            }

            query.setParameter("productState", productState);

            if (addProductDto.getState() != null) {
                query.setParameter("state", addProductDto.getState());
            }

            if (addProductDto.getExamDateTo() != null) {
                query.setParameter("examDateTo", new Timestamp(addProductDto.getExamDateTo().getTime()));
            }

            if (addProductDto.getGoLiveDate() != null) {
                query.setParameter("goLiveDate", new Timestamp(addProductDto.getGoLiveDate().getTime()));
            }

            if (addProductDto.getPlatformFee() != null) {
                query.setParameter("platformFee", addProductDto.getPlatformFee());
            }

            if (addProductDto.getPriorityLevel() != null) {
                query.setParameter("priorityLevel", addProductDto.getPriorityLevel());
            }

            if (addProductDto.getAdmitCardDateFrom() != null) {
                query.setParameter("admitCardDateFrom", new Timestamp(addProductDto.getAdmitCardDateFrom().getTime()));
            }

            if (addProductDto.getAdmitCardDateTo() != null) {
                query.setParameter("admitCardDateTo", new Timestamp(addProductDto.getAdmitCardDateTo().getTime()));
            }

            if (addProductDto.getModificationDateFrom() != null) {
                query.setParameter("modificationDateFrom", new Timestamp(addProductDto.getModificationDateFrom().getTime()));
            }

            if (addProductDto.getModificationDateTo() != null) {
                query.setParameter("modificationDateTo", new Timestamp(addProductDto.getModificationDateTo().getTime()));
            }

            if (addProductDto.getLastDateToPayFee() != null) {
                query.setParameter("lastDateToPayFee", new Timestamp(addProductDto.getLastDateToPayFee().getTime()));
            }

            if (addProductDto.getDownloadNotificationLink() != null) {
                query.setParameter("downloadNotificationLink", addProductDto.getDownloadNotificationLink());
            }

            if (addProductDto.getDownloadSyllabusLink() != null) {
                query.setParameter("downloadSyllabusLink", addProductDto.getDownloadSyllabusLink());
            }

            if (addProductDto.getFormComplexity() != null) {
                query.setParameter("formComplexity", addProductDto.getFormComplexity());
            }

            if (addProductDto.getGenderSpecific() != null) {
                query.setParameter("genderSpecificId", addProductDto.getGenderSpecific());
            }

            if (addProductDto.getSector() != null) {
                query.setParameter("sectorId", addProductDto.getSector());
            }

            if (addProductDto.getSelectionCriteria() != null) {
                query.setParameter("selectionCriteria", addProductDto.getSelectionCriteria());
            }

            if (addProductDto.getQualification() != null) {
                query.setParameter("qualificationId", addProductDto.getQualification());
            }

            if (addProductDto.getStream() != null) {
                query.setParameter("streamId", addProductDto.getStream());
            }

            if (addProductDto.getSubject() != null) {
                query.setParameter("subjectId", addProductDto.getSubject());
            }

            // Execute the update
            query.executeUpdate();

        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            throw new RuntimeException("Failed to save Custom Product: " + e.getMessage(), e);
        }
    }


    public List<CustomProduct> getCustomProducts() throws Exception {
        try {
            String sql = "SELECT * FROM custom_product";
            return entityManager.createNativeQuery(sql, CustomProduct.class).getResultList();

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Failed to retrieve CustomProducts: " + exception.getMessage(), exception);
        }
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
                CustomProductState productState = productStateService.getProductStateById(id);
                if (productState == null) {
                    throw new IllegalArgumentException("NO PRODUCT STATE FOUND WITH THIS ID: " + id);
                }
                customProductStates.add(productState);
            }
            jpql.append("AND p.productState IN :states ");
        }

        if (categories != null && !categories.isEmpty()) {
            for (Long id : categories) {
                Category category = catalogService.findCategoryById(id);
                if (category == null) {
                    throw new IllegalArgumentException("NO CATEGORY FOUND WITH THIS ID: " + id);
                }
                categoryList.add(category);
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

        // Execute and return the result
        return query.getResultList();
    }

    public List<CustomProduct> filterProductsByRoleAndUserId(Integer roleId, Long userId, int page, int limit) {
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT p FROM CustomProduct p JOIN p.creatoRole r ");

        Map<String, Object> queryParams = new HashMap<>();

        // Check if the role exists
        if (roleId != null) {
            Role role = entityManager.find(Role.class, roleId);
            if (role == null) {
                throw new IllegalArgumentException("No role exists with id " + roleId);
            }

            if (!role.getRole_name().equalsIgnoreCase(ADMIN) && !role.getRole_name().equalsIgnoreCase(SUPER_ADMIN)) {
                String roleCheckQuery = "SELECT COUNT(p) FROM CustomProduct p WHERE p.creatoRole.role_id = :roleId";
                Long roleProductCount = entityManager.createQuery(roleCheckQuery, Long.class)
                        .setParameter("roleId", roleId)
                        .getSingleResult();

                if (roleProductCount == 0) {
                    throw new IllegalArgumentException("No product is created by role with id " + roleId);
                } else {
                    jpql.append("WHERE r.role_id = :roleId ");
                    queryParams.put("roleId", roleId);
                }

                if (userId != null) {
                    String userCheckQuery = "SELECT COUNT(p) FROM CustomProduct p WHERE p.userId = :userId";
                    Long userProductCount = entityManager.createQuery(userCheckQuery, Long.class)
                            .setParameter("userId", userId)
                            .getSingleResult();

                    if (userProductCount == 0) {
                        throw new IllegalArgumentException("No user with id " + userId + " has created any product");
                    } else {
                        jpql.append("AND p.userId = :userId ");
                        queryParams.put("userId", userId);
                    }
                }
            } else {
                // For Admin or Superadmin, they can see all products, so no need to append any conditions
                jpql.append("WHERE 1=1 ");
            }
        }

        // Execute the query with pagination
        TypedQuery<CustomProduct> query = entityManager.createQuery(jpql.toString(), CustomProduct.class);
        queryParams.forEach(query::setParameter);

        int startPosition = page * limit;
        query.setFirstResult(startPosition);
        query.setMaxResults(limit);

        return query.getResultList();
    }

    public long countTotalProducts(Integer roleId, Long userId) {
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(DISTINCT p) FROM CustomProduct p JOIN p.creatoRole r ");

        Map<String, Object> queryParams = new HashMap<>();

        if (roleId != null) {
            Role role = entityManager.find(Role.class, roleId);
            if (role == null) {
                throw new IllegalArgumentException("No role exists with id " + roleId);
            }

            if (!role.getRole_name().equalsIgnoreCase(ADMIN) && !role.getRole_name().equalsIgnoreCase(SUPER_ADMIN)) {
                countJpql.append("WHERE r.role_id = :roleId ");
                queryParams.put("roleId", roleId);

                if (userId != null) {
                    countJpql.append("AND p.userId = :userId ");
                    queryParams.put("userId", userId);
                }
            } else {
                countJpql.append("WHERE 1=1 ");
            }
        }
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql.toString(), Long.class);
        queryParams.forEach(countQuery::setParameter);
        return countQuery.getSingleResult();
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
            if (categoryId <= 0) throw new IllegalArgumentException("Category id cannot be <= 0.");
            Category category = catalogService.findCategoryById(categoryId);
            if (category == null || ((Status) category).getArchived() == 'Y') {
                throw new IllegalArgumentException("Category not found with this Id.");
            }
            return category;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught while validating category: " + exception.getMessage() + "\n");
        }
    }

    public boolean addProductDtoValidation(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getQuantity() != null) {
                if (addProductDto.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity cannot be <= 0.");
                }
            } else {
                addProductDto.setQuantity(Constant.DEFAULT_QUANTITY);
            }

            if (addProductDto.getPlatformFee() != null) {
                if (addProductDto.getPlatformFee() <= 0) {
                    throw new IllegalArgumentException("Platform fee cannot be <= 0.");
                }
            } else {
                addProductDto.setPlatformFee(DEFAULT_PLATFORM_FEE);
            }

            if (addProductDto.getNotifyingAuthority() == null || addProductDto.getNotifyingAuthority().trim().isEmpty()) {
                throw new IllegalArgumentException("Notifying authority cannot be null");
            } else {
                addProductDto.setNotifyingAuthority(addProductDto.getNotifyingAuthority().trim());
            }

            if (addProductDto.getPriorityLevel() != null) {
                if (addProductDto.getPriorityLevel() <= 0 || addProductDto.getPriorityLevel() > 5) {
                    throw new IllegalArgumentException("Priority level must lie between 1-5.");
                }
            } else {
                addProductDto.setPriorityLevel(DEFAULT_PRIORITY_LEVEL);
            }

            if (addProductDto.getMetaTitle() == null || addProductDto.getMetaTitle().trim().isEmpty()) {
                throw new IllegalArgumentException(PRODUCTTITLENOTGIVEN);
            } else {
                addProductDto.setPostName(addProductDto.getMetaTitle().trim());
                addProductDto.setMetaTitle(addProductDto.getMetaTitle().trim());
            }

            if (addProductDto.getDisplayTemplate() == null || addProductDto.getDisplayTemplate().trim().isEmpty()) {
                addProductDto.setDisplayTemplate(addProductDto.getMetaTitle());
            } else {
                addProductDto.setDisplayTemplate(addProductDto.getDisplayTemplate().trim());
            }

            if (addProductDto.getMetaDescription() == null || addProductDto.getMetaDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Description cannot be null or empty.");
            } else {
                addProductDto.setMetaDescription(addProductDto.getMetaDescription().trim());
            }

            if(addProductDto.getPostName() == null || addProductDto.getPostName().trim().isEmpty()) {
                throw new IllegalArgumentException("Post Name cannot be null or empty.");
            } else {
                addProductDto.setPostName(addProductDto.getPostName().trim());
            }

            String formattedDate = dateFormat.format(new Date());
            Date activeStartDate = dateFormat.parse(formattedDate); // Convert formatted date string back to Date

            if (addProductDto.getActiveEndDate() == null || addProductDto.getGoLiveDate() == null || addProductDto.getActiveStartDate() == null) {
                throw new IllegalArgumentException("Active start date, active end date, and go live date cannot be empty.");
            }
            dateFormat.parse(dateFormat.format(addProductDto.getActiveStartDate()));
            dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
            dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));

            if (!addProductDto.getActiveEndDate().after(activeStartDate)) {
                throw new IllegalArgumentException("Expiration date cannot be before or equal of current date.");
            } else if (!addProductDto.getGoLiveDate().before(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Go live date cannot be after or equal of active end date.");
            } else if (!addProductDto.getActiveStartDate().before(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Active start date cannot be after or equal of active end date.");
            } else if (addProductDto.getGoLiveDate().before(new Date())) {
                throw new IllegalArgumentException("Go live date cannot be past of current date.");
            }

            if (addProductDto.getExamDateFrom() == null && addProductDto.getExamDateTo() == null) {
                throw new IllegalArgumentException("Both tentative examination date from-to cannot be null.");
            }
            if (addProductDto.getExamDateFrom() != null && addProductDto.getExamDateTo() == null) {
                addProductDto.setExamDateTo(addProductDto.getExamDateFrom());
            }
            if (addProductDto.getExamDateTo() != null && addProductDto.getExamDateFrom() == null) {
                addProductDto.setExamDateFrom(addProductDto.getExamDateTo());
            }

            dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
            dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));

            if (!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate()) || !addProductDto.getExamDateTo().after(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException(TENTATIVEDATEAFTERACTIVEENDDATE);
            } else if (addProductDto.getExamDateTo().before(addProductDto.getExamDateFrom())) {
                throw new IllegalArgumentException(TENTATIVEEXAMDATETOAFTEREXAMDATEFROM);
            }

            if (addProductDto.getJobGroup() == null || addProductDto.getJobGroup() <= 0) {
                throw new IllegalArgumentException("Job group cannot be null or <= 0.");
            }

            CustomJobGroup jobGroup = jobGroupService.getJobGroupById(addProductDto.getJobGroup());
            if (jobGroup == null) {
                throw new NoSuchElementException("Job group not found.");
            }

            if (addProductDto.getAdvertiserUrl() == null || addProductDto.getAdvertiserUrl().trim().isEmpty()) {
                throw new IllegalArgumentException("Advertiser url cannot be null or empty.");
            }
            addProductDto.setAdvertiserUrl(addProductDto.getAdvertiserUrl().trim());

            if (addProductDto.getApplicationScope() == null || addProductDto.getApplicationScope() <= 0) {
                throw new IllegalArgumentException("Application scope cannot be null or <= 0.");
            }

            CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(addProductDto.getApplicationScope());
            if (applicationScope == null) {
                throw new NoSuchElementException("application scope not found.");
            }

            if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_CENTER)) {

                if (addProductDto.getState() != null) {
                    throw new IllegalArgumentException("State cannot be given if application scope " + applicationScope.getApplicationScope());
                }
                if (addProductDto.getDomicileRequired() != null && addProductDto.getDomicileRequired()) {
                    throw new IllegalArgumentException("Domicile required cannot be true if application scope " + applicationScope.getApplicationScope());
                }
                addProductDto.setDomicileRequired(false);

            } else if (applicationScope.getApplicationScope().equals(APPLICATION_SCOPE_STATE)) {
                if (addProductDto.getDomicileRequired() == null || addProductDto.getState() == null) {
                    throw new IllegalArgumentException("For application scope: " + applicationScope.getApplicationScope() + " domicile and state cannot be null.");
                }

                if (addProductDto.getState() <= 0) {
                    throw new IllegalArgumentException("State cannot be <= 0.");
                }

                StateCode state = districtService.getStateByStateId(addProductDto.getState());
                if (state == null) {
                    throw new NoSuchElementException("State not found.");
                }
            }

            if (addProductDto.getReservedCategory() == null || addProductDto.getReservedCategory().isEmpty()) {
                throw new IllegalArgumentException("Reserve category must not be null or empty.");
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (NoSuchElementException noSuchElementException) {
            exceptionHandlingService.handleException(noSuchElementException);
            throw new IllegalArgumentException(noSuchElementException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new ParseException(parseException.getMessage() + "\n", parseException.getErrorOffset());
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage() + "\n");
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
                throw new IllegalArgumentException("Reserve category cannot be empty.");
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
                    throw new IllegalArgumentException("Reserve category id cannot be null or <= 0.");
                }
                reserveCategoryId.add(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());

                CustomReserveCategory reserveCategory = reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());
                if (reserveCategory == null) {
                    throw new IllegalArgumentException("Reserve category not found with id: " + addProductDto.getReservedCategory().get(reserveCategoryIndex).getReserveCategory());
                }

                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getFee() == null || addProductDto.getReservedCategory().get(reserveCategoryIndex).getFee() <= 0) {
                    throw new IllegalArgumentException("Fee cannot be null or <= 0.");
                }

                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getPost() == null) {
                    addProductDto.getReservedCategory().get(reserveCategoryIndex).setPost(Constant.DEFAULT_QUANTITY);
                } else if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getPost() <= 0) {
                    throw new IllegalArgumentException(POSTLESSTHANORZERO);
                }

                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore() == null || addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter() == null) {
                    throw new IllegalArgumentException("Born before date and born after date cannot be empty.");
                }

                dateFormat.parse(dateFormat.format(addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter()));
                dateFormat.parse(dateFormat.format(addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore()));

                if (!addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore().before(new Date()) || !addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter().before(new Date())) {
                    throw new IllegalArgumentException("Born before date and born after date must be of past.");
                } else if (!addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter().before(addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore())) {
                    throw new IllegalArgumentException("Born after date must be past of born before date.");
                }

                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornAfter().before(minBornAfterDate)) {
                    throw new IllegalArgumentException("Born after date cannot be more than 105 years in the past.");
                }
                if (addProductDto.getReservedCategory().get(reserveCategoryIndex).getBornBefore().after(maxBornBeforeDate)) {
                    throw new IllegalArgumentException("Born before date must be at least 5 years in the past.");
                }
            }

            if (reserveCategoryId.size() != addProductDto.getReservedCategory().size()) {
                throw new IllegalArgumentException("Duplicate reserve categories not allowed.");
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating reserve category: " + exception.getMessage() + "\n");
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
            if (!customProduct.getProductState().getProductState().equals(PRODUCT_STATE_MODIFIED) && !customProduct.getProductState().getProductState().equals(PRODUCT_STATE_NEW)) {
                throw new IllegalArgumentException("PRODUCT CAN ONLY BE MODIFIED IF IT IS IN NEW AND MODIFIED STATE");
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
                if (addProductDto.getPriorityLevel() <= 0 || addProductDto.getPriorityLevel() > 5) {
                    throw new IllegalArgumentException("PRIORITY LEVEL MUST BE BETWEEN 1-5");
                }
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
                } else if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_STATE) && customProduct.getCustomApplicationScope().getApplicationScope().equals(Constant.APPLICATION_SCOPE_STATE)) {
                    if (addProductDto.getState() != null && districtService.getStateByStateId(addProductDto.getState()) != null) {
                        customProduct.setState(districtService.getStateByStateId(addProductDto.getState()));
                        customProduct.setCustomApplicationScope(applicationScope);
                    } else {
                        throw new IllegalArgumentException("STATE NOT FOUND");
                    }

                    if (addProductDto.getDomicileRequired() != null) {
                        customProduct.setDomicileRequired(addProductDto.getDomicileRequired());
                        customProduct.setCustomApplicationScope(applicationScope);
                    }
                } else if (applicationScope.getApplicationScope().equals(Constant.APPLICATION_SCOPE_STATE) && customProduct.getCustomApplicationScope().getApplicationScope().equals(Constant.APPLICATION_SCOPE_CENTER)) {
                    if (addProductDto.getState() == null || addProductDto.getDomicileRequired() == null) {
                        throw new IllegalArgumentException("DOMICILE AND STATE ARE REQUIRED FIELDS FOR STATE APPLICATION SCOPE");
                    }

                    if (districtService.getStateByStateId(addProductDto.getState()) != null) {
                        customProduct.setState(districtService.getStateByStateId(addProductDto.getState()));
                    } else {
                        throw new IllegalArgumentException("STATE IS NOT FOUND");
                    }
                    customProduct.setDomicileRequired(addProductDto.getDomicileRequired());
                    customProduct.setCustomApplicationScope(applicationScope);
                } else if (applicationScope.getApplicationScope().equals(APPLICATION_SCOPE_CENTER)) {
                    if (addProductDto.getState() != null) {
                        throw new IllegalArgumentException("STATE NOT REQUIRED IN CASE OF CENTER LEVEL APPLICATION SCOPE");
                    }
                    if (addProductDto.getDomicileRequired() != null && addProductDto.getDomicileRequired()) {
                        throw new IllegalArgumentException("DOMICILE IS NOT REQUIRED IN CASE OF CENTER APPLICATION SCOPE");
                    }

                    addProductDto.setDomicileRequired(false);
                    addProductDto.setState(null);
                    customProduct.setState(null);
                    customProduct.setDomicileRequired(addProductDto.getDomicileRequired());
                    customProduct.setCustomApplicationScope(applicationScope);
                }
            }

            if (addProductDto.getAdvertiserUrl() != null) {
                if(!addProductDto.getAdvertiserUrl().trim().isEmpty()) {
                    addProductDto.setAdvertiserUrl(addProductDto.getAdvertiserUrl().trim());
                    customProduct.setAdvertiserUrl(addProductDto.getAdvertiserUrl());
                }else {
                    throw new IllegalArgumentException("Adviser Url cannot be empty");
                }
            }

            if(addProductDto.getNotifyingAuthority() != null) {
                if(!addProductDto.getNotifyingAuthority().trim().isEmpty()) {
                    addProductDto.setNotifyingAuthority(addProductDto.getNotifyingAuthority().trim());
                    customProduct.setNotifyingAuthority(addProductDto.getNotifyingAuthority());
                }else {
                    throw new IllegalArgumentException("Notifying authority cannot be empty");
                }
            }

            if(addProductDto.getPostName() != null) {
                if(!addProductDto.getPostName().trim().isEmpty()) {
                    addProductDto.setPostName(addProductDto.getPostName().trim());
                    customProduct.setPostName(addProductDto.getPostName());
                }else {
                    throw new IllegalArgumentException("Post name cannot be empty");
                }
            }

            if(addProductDto.getState() != null) {
                CustomSector customSector = sectorService.getSectorBySectorId(addProductDto.getSector());
                customProduct.setSector(customSector);
            }

            if(addProductDto.getStream() != null) {
                CustomStream customStream = streamService.getStreamByStreamId(addProductDto.getStream());
                customProduct.setStream(customStream);
            }

            if(addProductDto.getSubject() != null) {
                CustomSubject customSubject = subjectService.getSubjectBySubjectId(addProductDto.getSubject());
                customProduct.setSubject(customSubject);
            }

            if(addProductDto.getFormComplexity() != null) {
                if(addProductDto.getFormComplexity() < 0 || addProductDto.getFormComplexity() > 5) {
                    throw new IllegalArgumentException("Form complexity must lie between 1 and 5");
                }
                customProduct.setFormComplexity(addProductDto.getFormComplexity());
            }

            if(addProductDto.getSelectionCriteria() != null) {
                if(addProductDto.getSelectionCriteria().trim().isEmpty()) {
                    throw new IllegalArgumentException("Selection criteria cannot be empty");
                }
                customProduct.setSelectionCriteria(addProductDto.getSelectionCriteria());
            }

            if(addProductDto.getSector() != null) {
                CustomSector customSector = sectorService.getSectorBySectorId(addProductDto.getSector());
                customProduct.setSector(customSector);
            }

            if(addProductDto.getDownloadNotificationLink() != null) {
                if(addProductDto.getDownloadNotificationLink().trim().isEmpty()) {
                    throw new IllegalArgumentException("Download notification link cannot be empty");
                }
                addProductDto.setDownloadNotificationLink(addProductDto.getDownloadNotificationLink().trim());
                customProduct.setDownloadNotificationLink(addProductDto.getDownloadNotificationLink());
            }

            if(addProductDto.getDownloadSyllabusLink() != null) {
                if(addProductDto.getDownloadSyllabusLink().trim().isEmpty()) {
                    throw new IllegalArgumentException("Download syllabus link cannot be empty");
                }
                addProductDto.setDownloadSyllabusLink(addProductDto.getDownloadSyllabusLink().trim());
                customProduct.setDownloadSyllabusLink(addProductDto.getDownloadSyllabusLink());
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new Exception(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATION: " + exception.getMessage() + "\n");
        }
    }

    public Boolean validateAndSetActiveStartDate(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getActiveStartDate() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getActiveStartDate()));

                if (addProductDto.getActiveEndDate() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
                    if (!addProductDto.getActiveStartDate().before(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("Active start date must be before active end date.");
                    }
                } else {
                    if (!addProductDto.getActiveStartDate().before(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException("Active start date must be before active end date.");
                    }
                }
                customProduct.setActiveStartDate(addProductDto.getActiveStartDate());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating active start date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean validateAndSetGoLiveDate(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getGoLiveDate() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));

                if(addProductDto.getGoLiveDate().before(createdDate)) {
                    throw new IllegalArgumentException("Go live date must be after current date.");
                }

                if (addProductDto.getActiveEndDate() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
                    if (!addProductDto.getGoLiveDate().before(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("Go live date must be before active end date.");
                    }
                } else {
                    if (!addProductDto.getGoLiveDate().before(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException("Go live date must be before active end date.");
                    }
                }
                customProduct.setGoLiveDate(addProductDto.getGoLiveDate());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating go live date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean validateAndSetActiveEndDate(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getActiveEndDate() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));

                if (addProductDto.getLastDateToPayFee() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
                    if (!addProductDto.getActiveEndDate().before(addProductDto.getLastDateToPayFee())) {
                        throw new IllegalArgumentException("active end date have to be before of last date to pay fee.");
                    }
                } else if (addProductDto.getModificationDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getModificationDateFrom()));
                    if (!addProductDto.getActiveEndDate().before(addProductDto.getModificationDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of modification date from.");
                    }
                }else if (addProductDto.getAdmitCardDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateFrom()));
                    if (!addProductDto.getActiveEndDate().before(addProductDto.getAdmitCardDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of admit card from.");
                    }
                } else if (addProductDto.getExamDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                    if (!addProductDto.getActiveEndDate().before(addProductDto.getExamDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of exam date from.");
                    }
                } else if (customProduct.getLateDateToPayFee() != null) {
                    if (!addProductDto.getActiveEndDate().before(customProduct.getLateDateToPayFee())) {
                        throw new IllegalArgumentException("active end date have to be before of last date to pay fee.");
                    }
                } else if (customProduct.getModificationDateFrom() != null) {
                    if (!addProductDto.getActiveEndDate().before(customProduct.getModificationDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of modification date from.");
                    }
                } else if (customProduct.getAdmitCardDateFrom() != null) {
                    if (!addProductDto.getActiveEndDate().before(customProduct.getAdmitCardDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of admit card from.");
                    }
                } else if (customProduct.getExamDateFrom() != null) {
                    if (!addProductDto.getActiveEndDate().before(customProduct.getExamDateFrom())) {
                        throw new IllegalArgumentException("active end date have to be before of exam date from.");
                    }
                }
                customProduct.setActiveEndDate(addProductDto.getActiveEndDate());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating active start date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean validateAndSetLastDateToPayFeeDate(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getLastDateToPayFee() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));

                if(addProductDto.getActiveEndDate() != null) {
                    if(addProductDto.getLastDateToPayFee().before(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("Last day to pay fee cannot be before of active end date.");
                    }
                } else if(customProduct.getActiveEndDate() != null) {
                    if(addProductDto.getLastDateToPayFee().before(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException("Last day to pay fee cannot be before of active end date.");
                    }
                }
                if (addProductDto.getModificationDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
                    if (!addProductDto.getLastDateToPayFee().before(addProductDto.getModificationDateFrom())) {
                        throw new IllegalArgumentException("last date to pay fee have to be before of modified date from.");
                    }
                } else if (customProduct.getModificationDateFrom() != null) {
                    if (!addProductDto.getLastDateToPayFee().before(customProduct.getModificationDateFrom())) {
                        throw new IllegalArgumentException("last date to pay fee have to be before of modified date from.");
                    }
                }
                if (addProductDto.getAdmitCardDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
                    if (!addProductDto.getLastDateToPayFee().before(addProductDto.getAdmitCardDateFrom())) {
                        throw new IllegalArgumentException("last date to pay fee have to be before of admit card from.");
                    }
                } else if (customProduct.getAdmitCardDateFrom() != null) {
                    if (!addProductDto.getLastDateToPayFee().before(customProduct.getAdmitCardDateFrom())) {
                        throw new IllegalArgumentException("last date to pay fee have to be before of admit card from.");
                    }
                }
                if (addProductDto.getExamDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
                    if (!addProductDto.getLastDateToPayFee().before(addProductDto.getExamDateFrom())) {
                        throw new IllegalArgumentException("last date to pay fee have to be before of exam date from.");
                    }
                } else if (customProduct.getExamDateFrom() != null) {
                    if (!addProductDto.getLastDateToPayFee().before(customProduct.getExamDateFrom())) {
                        throw new IllegalArgumentException("last date to pay fee have to be before of exam date from.");
                    }
                }
                customProduct.setLateDateToPayFee(addProductDto.getLastDateToPayFee());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating active start date: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean validateAndSetModifiedDates(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if(addProductDto.getModificationDateFrom() != null && addProductDto.getModificationDateTo() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getModificationDateFrom()));
                dateFormat.parse(dateFormat.format(addProductDto.getModificationDateTo()));
            }else if(addProductDto.getModificationDateFrom() != null){
                dateFormat.parse(dateFormat.format(addProductDto.getModificationDateFrom()));
                addProductDto.setModificationDateTo(addProductDto.getModificationDateFrom());
            }

            if (addProductDto.getModificationDateFrom() != null && addProductDto.getModificationDateTo() != null) {

                if (addProductDto.getModificationDateFrom().after(addProductDto.getModificationDateTo())) {
                    throw new IllegalArgumentException("Modified date from must be before or equal of modified date to.");
                } else if(addProductDto.getLastDateToPayFee() != null) {
                    if (!addProductDto.getModificationDateFrom().after(addProductDto.getLastDateToPayFee())) {
                        throw new IllegalArgumentException("Modified date from must be after last date to pay fee.");
                    }
                } else if (customProduct.getLateDateToPayFee() != null) {
                    if (!addProductDto.getModificationDateFrom().after(customProduct.getLateDateToPayFee())) {
                        throw new IllegalArgumentException("Modified date from must be after last date to pay fee.");
                    }
                }
                if(addProductDto.getActiveEndDate() != null) {
                    if (!addProductDto.getModificationDateFrom().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("Modified date from must be after active end date.");
                    }
                } else if(customProduct.getActiveEndDate() != null) {
                    if (!addProductDto.getModificationDateFrom().after(customProduct.getLateDateToPayFee())) {
                        throw new IllegalArgumentException("Modified date from must be after active end date.");
                    }
                }
                if (addProductDto.getAdmitCardDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateFrom()));
                    if (!addProductDto.getModificationDateTo().before(addProductDto.getAdmitCardDateFrom())) {
                        throw new IllegalArgumentException("Modified date to must be before or equal of admit card date from.");
                    }
                } else if (customProduct.getAdmitCardDateFrom() != null) {
                    if (!addProductDto.getModificationDateTo().before(customProduct.getAdmitCardDateFrom())) {
                        throw new IllegalArgumentException("Modified date to must be before or equal of admit card date from.");
                    }
                }
                if (addProductDto.getExamDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));
                    if (!addProductDto.getModificationDateTo().before(addProductDto.getExamDateFrom())) {
                        throw new IllegalArgumentException("Modified date to must be before or equal of exam date from.");
                    }
                } else if (customProduct.getExamDateFrom() != null) {
                    if (!addProductDto.getModificationDateTo().before(customProduct.getExamDateFrom())) {
                        throw new IllegalArgumentException("Modified date to must be before or equal of exam date from.");
                    }
                }
                customProduct.setModificationDateFrom(addProductDto.getModificationDateFrom());
                customProduct.setModificationDateTo(addProductDto.getModificationDateTo());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating modification dates: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean validateAndSetAdmitCardDates(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if(addProductDto.getAdmitCardDateFrom() != null && addProductDto.getAdmitCardDateTo() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateFrom()));
                dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateTo()));
            }else if(addProductDto.getAdmitCardDateFrom() != null){
                dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateFrom()));
                addProductDto.setAdmitCardDateTo(addProductDto.getAdmitCardDateFrom());
            }

            if (addProductDto.getAdmitCardDateFrom() != null && addProductDto.getAdmitCardDateTo() != null) {

                if (addProductDto.getAdmitCardDateFrom().after(addProductDto.getAdmitCardDateTo())) {
                    throw new IllegalArgumentException("Admit card date from must be before or equal of admit card date to.");
                } else if(addProductDto.getModificationDateTo() != null){
                    if(!addProductDto.getAdmitCardDateFrom().after(addProductDto.getModificationDateTo())) {
                        throw new IllegalArgumentException("Admit card date from must be after modification date to.");
                    }
                } else if(customProduct.getModificationDateTo() != null) {
                    if(!addProductDto.getAdmitCardDateFrom().after(customProduct.getModificationDateTo())) {
                        throw new IllegalArgumentException("Admit card date from must be after modification date .");
                    }
                }
                if(addProductDto.getLastDateToPayFee() != null) {
                    if(!addProductDto.getAdmitCardDateFrom().after(addProductDto.getModificationDateTo())) {
                        throw new IllegalArgumentException("Admit card date from must be after last date to pay fee.");
                    }
                } else if(customProduct.getLateDateToPayFee() != null) {
                    if(!addProductDto.getAdmitCardDateFrom().after(customProduct.getModificationDateTo())) {
                        throw new IllegalArgumentException("Admit card date from must be after last date to pay fee.");
                    }
                }
                if(addProductDto.getActiveEndDate() != null) {
                    if(!addProductDto.getAdmitCardDateFrom().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("Admit card date from must be after active end date.");
                    }
                } else if(customProduct.getActiveEndDate() != null) {
                    if(!addProductDto.getAdmitCardDateFrom().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException("Admit card date from must be after active end date.");
                    }
                }
                if (addProductDto.getExamDateFrom() != null) {
                    dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));
                    if (!addProductDto.getAdmitCardDateTo().before(addProductDto.getExamDateTo())) {
                        throw new IllegalArgumentException("Admit date to must be before or equal of exam date from.");
                    }
                } else if (customProduct.getExamDateFrom() != null) {
                    if (!addProductDto.getAdmitCardDateTo().before(customProduct.getExamDateFrom())) {
                        throw new IllegalArgumentException("Admit card date to must be before or equal of exam date from.");
                    }
                }
                customProduct.setAdmitCardDateFrom(addProductDto.getAdmitCardDateFrom());
                customProduct.setAdmitCardDateTo(addProductDto.getAdmitCardDateTo());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating admit card dates: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean validateAndSetExamDates(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if(addProductDto.getExamDateFrom() != null && addProductDto.getExamDateTo() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateTo()));
            }else if(addProductDto.getExamDateFrom() != null){
                dateFormat.parse(dateFormat.format(addProductDto.getExamDateFrom()));
                addProductDto.setExamDateTo(addProductDto.getExamDateFrom());
            }

            if (addProductDto.getExamDateFrom() != null && addProductDto.getExamDateTo() != null) {
                if (addProductDto.getExamDateFrom().after(addProductDto.getExamDateTo())) {
                    throw new IllegalArgumentException("Exam date from must be before or equal of exam date to.");
                } else if (addProductDto.getAdmitCardDateTo() != null){
                    if(!addProductDto.getExamDateFrom().after(addProductDto.getAdmitCardDateTo())) {
                        throw new IllegalArgumentException("Exam date from must be after of admit card date to.");
                    }
                } else if(customProduct.getAdmitCardDateTo() != null) {
                    if(!addProductDto.getExamDateFrom().after(customProduct.getAdmitCardDateTo())) {
                        throw new IllegalArgumentException("Exam date from must be after of admit card to.");
                    }
                }
                if (addProductDto.getModificationDateTo() != null){
                    if(!addProductDto.getExamDateFrom().after(addProductDto.getModificationDateTo())) {
                        throw new IllegalArgumentException("Exam date from must be after of modified date to.");
                    }
                } else if(customProduct.getModificationDateTo() != null) {
                    if(!addProductDto.getExamDateFrom().after(customProduct.getModificationDateTo())) {
                        throw new IllegalArgumentException("Exam date from must be after of modified date to.");
                    }
                }
                if (addProductDto.getLastDateToPayFee() != null){
                    if(!addProductDto.getExamDateFrom().after(addProductDto.getLastDateToPayFee())) {
                        throw new IllegalArgumentException("Exam date from must be after of last date to pay fee.");
                    }
                } else if (customProduct.getLateDateToPayFee() != null){
                    if(!addProductDto.getExamDateFrom().after(customProduct.getLateDateToPayFee())) {
                        throw new IllegalArgumentException("Exam date from must be after of last date to pay fee.");
                    }
                }
                if (addProductDto.getActiveEndDate() != null){
                    if(!addProductDto.getExamDateFrom().after(addProductDto.getActiveEndDate())) {
                        throw new IllegalArgumentException("Exam date from must be after of active end date.");
                    }
                } else if (customProduct.getActiveEndDate() != null){
                    if(!addProductDto.getExamDateFrom().after(customProduct.getActiveEndDate())) {
                        throw new IllegalArgumentException("Exam date from must be after of active end date.");
                    }
                }
                customProduct.setExamDateFrom(addProductDto.getExamDateFrom());
                customProduct.setExamDateTo(addProductDto.getExamDateTo());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating exam dates: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public Boolean validateAndSetActiveStartDateActiveEndDateAndGoLiveDateFields(AddProductDto addProductDto, CustomProduct customProduct, Date createdDate) throws Exception {
        try {
            if (addProductDto.getActiveEndDate() != null && addProductDto.getGoLiveDate() != null) {

                dateFormat.parse(dateFormat.format(addProductDto.getActiveEndDate()));
                dateFormat.parse(dateFormat.format(addProductDto.getGoLiveDate()));

                if (addProductDto.getGoLiveDate().before(createdDate)) {
                    throw new IllegalArgumentException("GO LIVE DATE HAS TO OF FUTURE OF CURRENT DATE");
                } else if (!addProductDto.getActiveEndDate().after(customProduct.getActiveStartDate())) {
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

                if ((!customProduct.getProductState().getProductState().equals(Constant.PRODUCT_STATE_NEW) && !customProduct.getProductState().getProductState().equals(Constant.PRODUCT_STATE_MODIFIED)) || (!customProductState.getProductState().equals(PRODUCT_STATE_APPROVED) && !customProductState.getProductState().equals(PRODUCT_STATE_REJECTED))) {
                    throw new IllegalArgumentException("PRODUCT STATE ONLY CHANGE FROM NEW/MODIFIABLE TO APPROVED OR REJECTED STATE");
                }

                if (role.equals(Constant.SERVICE_PROVIDER)) {
                    List<Privileges> privileges = privilegeService.getServiceProviderPrivilege(userId);
                    for (Privileges privilege : privileges) {
                        if ((privilege.getPrivilege_name().equals(Constant.PRIVILEGE_APPROVE_PRODUCT) && customProductState.getProductState().equals(Constant.PRODUCT_STATE_APPROVED))) {
                            customProduct.setProductState(customProductState);
                            return true;
                        } else if ((privilege.getPrivilege_name().equals(Constant.PRIVILEGE_REJECT_PRODUCT) && customProductState.getProductState().equals(Constant.PRODUCT_STATE_REJECTED))) {

                            if (addProductDto.getRejectionStatus() == null) {
                                throw new IllegalArgumentException("REJECTION STATUS CANNOT BE NULL IF PRODUCT IS REJECTED");
                            }
                            CustomProductRejectionStatus productRejectionStatus = productRejectionStatusService.getAllRejectionStatusByRejectionStatusId(addProductDto.getRejectionStatus());
                            if (productRejectionStatus == null) {
                                throw new IllegalArgumentException("NO PRODUCT REJECTION STATUS IS FOUND");
                            }
                            customProduct.setProductState(customProductState);
                            customProduct.setRejectionStatus(productRejectionStatus);
                            return true;
                        }
                    }
                    throw new IllegalArgumentException("Not have privilege to perform action.");
                } else if (role.equals(Constant.ADMIN) || role.equals(Constant.SUPER_ADMIN)) {
                    if (addProductDto.getRejectionStatus() == null) {
                        throw new IllegalArgumentException("REJECTION STATE CANNOT BE NULL IF PRODUCT IS REJECTED");
                    }
                    CustomProductRejectionStatus productRejectionStatus = productRejectionStatusService.getAllRejectionStatusByRejectionStatusId(addProductDto.getRejectionStatus());
                    if (productRejectionStatus == null) {
                        throw new IllegalArgumentException("NO PRODUCT REJECTION STATUS IS FOUND");
                    }
                    customProduct.setRejectionStatus(productRejectionStatus);
                    customProduct.setProductState(customProductState);

                    return true;
                } else {
                    throw new IllegalArgumentException("Role not Service provider and admin or super admin");
                }
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage());
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

    public boolean deleteOldPhysicalRequirement(CustomProduct customProduct) throws Exception {
        try {
            productGenderPhysicalRequirementService.removeProductGenderPhysicalRequirementByProductId(customProduct);
            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean validateAdmitCardDates(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getAdmitCardDateFrom() == null && addProductDto.getAdmitCardDateTo() == null) {
                return true;
            }

            if (addProductDto.getAdmitCardDateFrom() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateFrom()));
            }
            if (addProductDto.getAdmitCardDateTo() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getAdmitCardDateTo()));
            }

            if (addProductDto.getAdmitCardDateFrom() != null && addProductDto.getAdmitCardDateTo() != null) {
                if (addProductDto.getAdmitCardDateFrom().after(addProductDto.getAdmitCardDateTo())) {
                    throw new IllegalArgumentException("Admit card date from cannot be of future of admit card date to.");
                }
            } else if (addProductDto.getAdmitCardDateFrom() != null) {
                addProductDto.setAdmitCardDateTo(addProductDto.getAdmitCardDateFrom());
            } else if (addProductDto.getAdmitCardDateTo() != null) {
                addProductDto.setAdmitCardDateFrom(addProductDto.getAdmitCardDateTo());
            }

            if (!addProductDto.getExamDateFrom().after(addProductDto.getAdmitCardDateTo())) {
                throw new IllegalArgumentException("Admit card to cannot be future of exam date from.");
            }

            if (addProductDto.getModificationDateTo() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getModificationDateTo()));
                if (!addProductDto.getAdmitCardDateFrom().after(addProductDto.getModificationDateTo())) {
                    throw new IllegalArgumentException("Admit card date from must be of future of modification date to.");
                }
            } else if (addProductDto.getLastDateToPayFee() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
                if (!addProductDto.getAdmitCardDateFrom().after(addProductDto.getLastDateToPayFee())) {
                    throw new IllegalArgumentException("Admit card date from must be of future of last date to pay application fee.");
                }
            } else {
                if (!addProductDto.getAdmitCardDateFrom().after(addProductDto.getActiveEndDate())) {
                    throw new IllegalArgumentException("Admit card date from must be of future of active end date.");
                }
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("PARSE EXCEPTION CAUGHT WHILE VALIDATING ADMIT CARD DATES: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }

    public boolean validateModificationDates(AddProductDto addProductDto) throws Exception {
        try {

            if (addProductDto.getModificationDateFrom() == null && addProductDto.getModificationDateTo() == null) {
                return true;
            }

            if (addProductDto.getModificationDateFrom() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getModificationDateFrom()));
            }
            if (addProductDto.getModificationDateTo() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getModificationDateTo()));
            }

            if (addProductDto.getModificationDateFrom() != null && addProductDto.getModificationDateTo() != null) {
                if (addProductDto.getModificationDateFrom().after(addProductDto.getModificationDateTo())) {
                    throw new IllegalArgumentException("Modification date from cannot be of future of modification date to.");
                }

            } else if (addProductDto.getAdmitCardDateFrom() != null) {
                addProductDto.setModificationDateTo(addProductDto.getModificationDateFrom());
            } else if (addProductDto.getAdmitCardDateTo() != null) {
                addProductDto.setModificationDateFrom(addProductDto.getModificationDateTo());
            }

            if (addProductDto.getAdmitCardDateFrom() != null) {
                if (addProductDto.getModificationDateTo().after(addProductDto.getAdmitCardDateFrom())) {
                    throw new IllegalArgumentException("Modification date to cannot be of future of admit card date from.");
                }
            } else {
                if (addProductDto.getModificationDateTo().after(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException("Modification date to cannot be of future of exam date from");
                }
            }

            if (addProductDto.getLastDateToPayFee() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));

                if (!addProductDto.getModificationDateFrom().after(addProductDto.getLastDateToPayFee())) {
                    throw new IllegalArgumentException("Modification date from has to be future of last date to pay application fee.");
                }
            } else {
                if (!addProductDto.getModificationDateFrom().after(addProductDto.getActiveEndDate())) {
                    throw new IllegalArgumentException("Modification date from has to be future of active end date.");
                }
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating modification dates: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception occurred while validating modification dates: " + exception.getMessage());
        }
    }

    public boolean validateLastDateToPayFee(AddProductDto addProductDto) throws Exception {
        try {

            if (addProductDto.getLastDateToPayFee() == null) {
                return true;
            }

            if (addProductDto.getLastDateToPayFee() != null) {
                dateFormat.parse(dateFormat.format(addProductDto.getLastDateToPayFee()));
            }

            if (addProductDto.getModificationDateFrom() != null) {
                if (addProductDto.getLastDateToPayFee().after(addProductDto.getModificationDateFrom())) {
                    throw new IllegalArgumentException("Last date to pay fee cannot be after or equal to modifying date from.");
                }
            } else if (addProductDto.getAdmitCardDateFrom() != null) {
                if (addProductDto.getLastDateToPayFee().after(addProductDto.getAdmitCardDateFrom())) {
                    throw new IllegalArgumentException("Last date to pay fee cannot be after or equal to admit card date from.");
                }
            } else {
                if (addProductDto.getLastDateToPayFee().after(addProductDto.getExamDateFrom())) {
                    throw new IllegalArgumentException("Last date to pay fee cannot be after or equal to exam date from.");
                }
            }

            if (!addProductDto.getLastDateToPayFee().after(addProductDto.getActiveEndDate())) {
                throw new IllegalArgumentException("Last date to pay application fee has to future of active end date");
            }

            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (ParseException parseException) {
            exceptionHandlingService.handleException(parseException);
            throw new Exception("Parse exception caught while validating last date to pay application fee: " + parseException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception occurred while validating last date to pay application fee: " + exception.getMessage());
        }
    }

    public boolean validateLinks(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getDownloadNotificationLink() != null) {
                if (addProductDto.getDownloadNotificationLink().trim().isEmpty()) {
                    throw new IllegalArgumentException("Notification download link cannot be empty");
                }
                addProductDto.setDownloadNotificationLink(addProductDto.getDownloadNotificationLink().trim());
            }

            if (addProductDto.getDownloadSyllabusLink() != null) {
                if (addProductDto.getDownloadSyllabusLink().trim().isEmpty()) {
                    throw new IllegalArgumentException("Syllabus download link cannot be empty.");
                }
                addProductDto.setDownloadSyllabusLink(addProductDto.getDownloadSyllabusLink().trim());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception occurred while validating links: " + exception.getMessage());
        }
    }

    public boolean validateFormComplexity(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getFormComplexity() == null) {
                addProductDto.setFormComplexity(1L);
            } else if (addProductDto.getFormComplexity() <= 0 || addProductDto.getFormComplexity() > 5) {
                throw new IllegalArgumentException("Form complexity must lie in range 1-5.");
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception occurred while validating form complexity: " + exception.getMessage());
        }
    }

    public boolean validatePhysicalRequirement(AddProductDto addProductDto, CustomProduct customProduct) throws Exception {
        try {
            CustomGender gender = null;
            if (addProductDto.getGenderSpecific() != null) {
                gender = genderService.getGenderByGenderId(addProductDto.getGenderSpecific());
            }
            if (addProductDto.getPhysicalRequirement() == null) {
                return true;
            }
            if (!addProductDto.getPhysicalRequirement().isEmpty()) {
                Set<Long> genderId = new HashSet<>();

                for (int physicalAttributeIndex = 0; physicalAttributeIndex < addProductDto.getPhysicalRequirement().size(); physicalAttributeIndex++) {
                    if (addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getGenderId() == null || addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getGenderId() <= 0) {
                        throw new IllegalArgumentException("GENDER ID CANNOT BE NULL OR <= 0");
                    }
                    genderId.add(addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getGenderId());

                    CustomGender customGender = genderService.getGenderByGenderId(addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getGenderId());
                    if (customGender == null) {
                        throw new IllegalArgumentException("GENDER NOT FOUND WITH ID: " + addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getGenderId());
                    }
                    if (addProductDto.getGenderSpecific() != null && customGender != gender) {
                        throw new IllegalArgumentException("Gender id is not matched with the specific gender.");
                    } else if (customProduct != null && customProduct.getGenderSpecific() != null && addProductDto.getGenderSpecific() == null && customGender != customProduct.getGenderSpecific()) {
                        throw new IllegalArgumentException("Gender id is not matched with the specific gender.");
                    }

                    if (addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getHeight() == null || addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getHeight() > Constant.MAX_HEIGHT || addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getHeight() < Constant.MIN_HEIGHT) {
                        throw new IllegalArgumentException("HEIGHT IS MANDATORY FIELD AND MUST BE LESS THAN " + MAX_HEIGHT + " AND GREATER THAN " + MIN_HEIGHT);
                    }
                    if (addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getWeight() == null || addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getWeight() > MAX_WEIGHT || addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getWeight() < MIN_HEIGHT) {
                        throw new IllegalArgumentException("WEIGHT IS MANDATORY FIELD AND MUST BE LESS THAN " + MAX_WEIGHT + " AND GREATER THAN " + MIN_WEIGHT);
                    }

                    if (addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getShoeSize() != null && (addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getShoeSize() > MAX_SHOE_SIZE || addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getShoeSize() < MIN_SHOE_SIZE)) {
                        throw new IllegalArgumentException("SHOE SIZE MUST BE LESS THAN " + MAX_SHOE_SIZE + " AND GREATER THAN " + MIN_SHOE_SIZE);
                    }
                    if (addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getWaistSize() != null && (addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getWaistSize() > MAX_WAIST_SIZE || addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getWaistSize() < MIN_WAIST_SIZE)) {
                        throw new IllegalArgumentException("WAIST SIZE MUST BE LESS THAN " + MAX_WAIST_SIZE + " AND GREATER THAN " + MIN_WAIST_SIZE);
                    }
                    if (addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getChestSize() != null && (addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getChestSize() > MAX_CHEST_SIZE || addProductDto.getPhysicalRequirement().get(physicalAttributeIndex).getChestSize() < MIN_CHEST_SIZE)) {
                        throw new IllegalArgumentException("CHEST SIZE MUST BE LESS THAN " + MAX_CHEST_SIZE + " AND GREATER THAN " + MIN_CHEST_SIZE);
                    }

                }

                if (genderId.size() != addProductDto.getPhysicalRequirement().size()) {
                    throw new IllegalArgumentException("DUPLICATE GENDER NOT ALLOWED");
                }
            }

            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION WHILE VALIDATING PHYSICAL REQUIREMENTS: " + exception.getMessage() + "\n");
        }
    }

    public CustomGender validateGenderSpecificField(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getGenderSpecific() != null) {
                CustomGender customGender = genderService.getGenderByGenderId(addProductDto.getGenderSpecific());
                if (customGender == null) {
                    throw new IllegalArgumentException("No gender found with this id.");
                }
                return customGender;
            }
            return null;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating gender specific id: " + exception.getMessage() + "\n");
        }
    }

    public CustomSector validateSector(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getSector() != null) {
                CustomSector customSector = sectorService.getSectorBySectorId(addProductDto.getSector());
                if (customSector == null) {
                    throw new IllegalArgumentException("No sector found with this id.");
                }
                return customSector;
            }
            return null;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating sector: " + exception.getMessage() + "\n");
        }
    }

    public Boolean validateSelectionCriteria(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getSelectionCriteria() != null) {
                if (addProductDto.getSelectionCriteria().trim().isEmpty()) {
                    throw new IllegalArgumentException("Selection criteria cannot be emptyse");
                }
                addProductDto.setSelectionCriteria(addProductDto.getSelectionCriteria().trim());
            }
            return true;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating selection criteria: " + exception.getMessage() + "\n");
        }
    }

    public Qualification validateQualification(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getQualification() != null) {
                Qualification qualification = qualificationService.getQualificationByQualificationId(addProductDto.getQualification());
                if (qualification == null) {
                    throw new IllegalArgumentException("Qualification not found with this id.");
                }
                return qualification;

            } else {
                throw new IllegalArgumentException("Qualification cannot be null.");
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating qualification: " + exception.getMessage() + "\n");
        }
    }

    public CustomStream validateStream(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getStream() != null) {
                CustomStream customStream = streamService.getStreamByStreamId(addProductDto.getStream());
                if (customStream == null) {
                    throw new IllegalArgumentException("Stream not found with this id.");
                }
                return customStream;

            }
            return null;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating stream: " + exception.getMessage() + "\n");
        }
    }

    public CustomSubject validateSubject(AddProductDto addProductDto) throws Exception {
        try {
            if (addProductDto.getSubject() != null) {
                CustomSubject customSubject = subjectService.getSubjectBySubjectId(addProductDto.getSubject());
                if (customSubject == null) {
                    throw new IllegalArgumentException("Subject not found with this id.");
                }
                return customSubject;
            }
            return null;
        } catch (IllegalArgumentException illegalArgumentException) {
            exceptionHandlingService.handleException(illegalArgumentException);
            throw new IllegalArgumentException(illegalArgumentException.getMessage() + "\n");
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Some exception while validating subject: " + exception.getMessage() + "\n");
        }
    }

}