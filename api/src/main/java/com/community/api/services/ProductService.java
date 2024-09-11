package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.AddProductDto;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.entity.*;
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
import java.util.List;
import java.util.Map;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.context.annotation.ApplicationScope;

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

    @PersistenceContext
    private EntityManager entityManager;

    @Resource(
            name = "blCatalogService"
    )
    protected CatalogService catalogService;

    public void saveCustomProduct(Product product, AddProductDto addProductDto, Date examDateFrom, Date examDateTo, Date goLiveDate, Double platformFee, Integer priorityLevel, CustomApplicationScope applicationScope, CustomJobGroup jobGroup, CustomProductState productState, Role role, Long userId, String notifyingAuthority, Date modifiedDate, Long modifierUserId, Role modifierRole) {

        String sql = "INSERT INTO custom_product (product_id, exam_date_from, exam_date_to, go_live_date, platform_fee, priority_level, application_scope_id, job_group_id, product_state_id, creator_role_id, creator_user_id, notifying_authority, last_modified, advertiser_url, domicile_required) VALUES (:productId, :examDateFrom, :examDateTo, :goLiveDate, :platformFee, :priorityLevel, :applicationScopeId, :jobGroupId, :productStateId, :roleId, :userId, :notifyingAuthority, :modifiedDate, :advertiserUrl, :domicileRequired)";

        try {
            entityManager.createNativeQuery(sql)
                    .setParameter("productId", product)
                    .setParameter("examDateFrom", examDateFrom != null ? new Timestamp(examDateFrom.getTime()) : null)
                    .setParameter("examDateTo", examDateTo != null ? new Timestamp(examDateTo.getTime()) : null)
                    .setParameter("goLiveDate", goLiveDate != null ? new Timestamp(goLiveDate.getTime()) : null)
                    .setParameter("platformFee", platformFee)
                    .setParameter("priorityLevel", priorityLevel)
                    .setParameter("applicationScopeId", applicationScope.getApplicationScopeId())
                    .setParameter("jobGroupId", jobGroup.getJobGroupId())
                    .setParameter("productStateId", productState.getProductStateId())
                    .setParameter("roleId", role.getRole_id())
                    .setParameter("userId", userId)
                    .setParameter("notifyingAuthority", notifyingAuthority)
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

//    @Query("SELECT p FROM CustomProduct p WHERE " +
////            "(:dateFrom IS NULL OR p.dateAdded >= :dateFrom) AND " +
////            "(:dateTo IS NULL OR p.dateAdded <= :dateTo) AND " +
//            ":states IS NULL OR p.state IN :states")
////            "(:categories IS NULL OR p.category IN :categories) AND " +
////            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
////            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
//    public List<CustomProduct> findFilteredProducts (@Param("states") List<CustomProductState> states);


    public List<CustomProduct> filterProducts(List<Long> states, List<Long> categories, String title, Double fee, Integer post) {
        List<CustomProductState> customProductStates = new ArrayList<>();
        for (Long id : states) {
            customProductStates.add(productStateService.getProductStateById(id));
        }
        List<Category> categoryList = new ArrayList<>();
        for (Long id : categories) {
            categoryList.add(catalogService.findCategoryById(id));
        }
        /*String jpql = "SELECT p FROM CustomProduct p WHERE p.productState IN :states AND p.defaultCategory IN :categories AND p.metaTitle LIKE :title"
                + " JOIN CustomProductReserveCategoryFeePostRef r ON p.productId = r.customProduct.productId "
                + "AND r.fee > :fee "
                + "AND r.post > :post ";*/

        String jpql = "SELECT DISTINCT p FROM CustomProduct p "
                + "JOIN CustomProductReserveCategoryFeePostRef r "
                + "ON r.customProduct = p "
                + "WHERE p.productState IN :states "
                + "AND p.defaultCategory IN :categories "
                + "AND p.metaTitle LIKE :title "
                + "AND r.fee > :fee ";
//                + "AND r.post > :post";


        TypedQuery<CustomProduct> query = entityManager.createQuery(jpql, CustomProduct.class);
        query.setParameter("states", customProductStates);
        query.setParameter("categories", categoryList);
        query.setParameter("title", "%"+title+"%");
        query.setParameter("fee", fee);
//        query.setParameter("post", post);

        return query.getResultList();

    }
}