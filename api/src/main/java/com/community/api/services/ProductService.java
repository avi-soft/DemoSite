package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.AddProductDto;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.entity.*;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.ProductImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.context.annotation.ApplicationScope;

import static com.community.api.endpoint.avisoft.controller.product.ProductController.FEELESSTHANOREQUALZERO;
import static com.community.api.endpoint.avisoft.controller.product.ProductController.POSTLESSTHANORZERO;

@Service
public class ProductService {

    @Autowired
    ReserveCategoryDtoService reserveCategoryDtoService;

    @Autowired
    ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;

    @Autowired
    ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;

    @PersistenceContext
    private EntityManager entityManager;

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

    public ResponseEntity<?> validateAndSetActiveEndDateAndGoLiveDateFields(AddProductDto addProductDto, CustomProduct customProduct, DateFormat dateFormat) {
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
            }
        } catch (ParseException e) {
            return ResponseService.generateErrorResponse("DATE PARSE EXCEPTION: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    /*private ResponseEntity<?> handleReserveCategoryUpdates(
            AddProductDto addProductDto,
            Long productId,
            CustomProduct customProduct,
            DateFormat dateFormat
    ) {
        try {

            List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(productId);
            boolean reserveCategoryFound = reserveCategoryDtoList.stream()
                    .anyMatch(dto -> dto.getReserveCategoryId().equals(addProductDto.getReservedCategory()));

            if (reserveCategoryFound) {
                CustomProductReserveCategoryBornBeforeAfterRef customProductReserveCategoryBornBeforeAfterRef =
                        productReserveCategoryBornBeforeAfterRefService.getCustomProductReserveCategoryBornBeforeAfterRefByProductIdAndReserveCategoryId(productId, addProductDto.getReservedCategory());

                CustomProductReserveCategoryFeePostRef customProductReserveCategoryFeePostRef =
                        productReserveCategoryFeePostRefService.getCustomProductReserveCategoryFeePostRefByProductIdAndReserveCategoryId(productId, addProductDto.getReservedCategory());

                if (addProductDto.getBornAfter() != null && addProductDto.getBornBefore() != null) {
                    validateAndUpdateBornDates(addProductDto, customProductReserveCategoryBornBeforeAfterRef, dateFormat);
                } else if (addProductDto.getBornAfter() != null) {
                    validateAndUpdateBornAfter(addProductDto, customProductReserveCategoryBornBeforeAfterRef, dateFormat);
                } else if (addProductDto.getBornBefore() != null) {
                    validateAndUpdateBornBefore(addProductDto, customProductReserveCategoryBornBeforeAfterRef, dateFormat);
                }

                if (addProductDto.getFee() != null) {
                    validateAndUpdateFee(addProductDto, customProductReserveCategoryFeePostRef);
                }

                if (addProductDto.getPost() != null) {
                    validateAndUpdatePost(addProductDto, customProductReserveCategoryFeePostRef);
                }

                entityManager.persist(customProductReserveCategoryBornBeforeAfterRef);
                entityManager.persist(customProductReserveCategoryFeePostRef);

            } else {
                validateNewReserveCategory(addProductDto);
                productReserveCategoryBornBeforeAfterRefService.saveBornBeforeAndBornAfter(
                        addProductDto.getBornBefore(),
                        addProductDto.getBornAfter(),
                        customProduct,
                        reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory())
                );
                productReserveCategoryFeePostRefService.saveFeeAndPost(
                        addProductDto.getFee(),
                        addProductDto.getPost(),
                        customProduct,
                        reserveCategoryService.getReserveCategoryById(addProductDto.getReservedCategory())
                );
            }

            return ResponseService.generateSuccessResponse("Product Updated Successfully",
                    new CustomProductWrapper().wrapDetails(customProduct, reserveCategoryDtoList),
                    HttpStatus.OK);

        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.NUMBER_FORMAT_EXCEPTION + ": " + numberFormatException.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private ResponseEntity<?> validateAndUpdateBornDates(
            AddProductDto addProductDto,
            CustomProductReserveCategoryBornBeforeAfterRef ref,
            DateFormat dateFormat
    ) {
        try {
            dateFormat.parse(dateFormat.format(addProductDto.getBornAfter()));
            dateFormat.parse(dateFormat.format(addProductDto.getBornBefore()));

            if (!addProductDto.getBornBefore().before(new Date()) || !addProductDto.getBornAfter().before(new Date())) {
                return ResponseService.generateErrorResponse("BORN BEFORE DATE AND BORN AFTER DATE MUST BE OF PAST", HttpStatus.BAD_REQUEST);
            } else if (!addProductDto.getBornAfter().before(addProductDto.getBornBefore())) {
                return ResponseService.generateErrorResponse("BORN AFTER DATE MUST BE PAST OF BORN BEFORE DATE", HttpStatus.BAD_REQUEST);
            }

            ref.setBornBefore(addProductDto.getBornBefore());
            ref.setBornAfter(addProductDto.getBornAfter());
            return null;

        } catch (ParseException e) {
            return ResponseService.generateErrorResponse("DATE PARSING ERROR: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<?> validateAndUpdateBornAfter(
            AddProductDto addProductDto,
            CustomProductReserveCategoryBornBeforeAfterRef ref,
            DateFormat dateFormat
    ) {
        try {
            dateFormat.parse(dateFormat.format(addProductDto.getBornAfter()));

            if (!addProductDto.getBornAfter().before(new Date())) {
                return ResponseService.generateErrorResponse("BORN AFTER DATE MUST BE OF PAST", HttpStatus.BAD_REQUEST);
            } else if (!addProductDto.getBornAfter().before(ref.getBornBefore())) {
                return ResponseService.generateErrorResponse("BORN AFTER DATE MUST BE PAST OF BORN BEFORE DATE", HttpStatus.BAD_REQUEST);
            }

            ref.setBornAfter(addProductDto.getBornAfter());
            return null;

        } catch (ParseException e) {
            return ResponseService.generateErrorResponse("DATE PARSING ERROR: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<?> validateAndUpdateBornBefore(
            AddProductDto addProductDto,
            CustomProductReserveCategoryBornBeforeAfterRef ref,
            DateFormat dateFormat
    ) {
        try {
            dateFormat.parse(dateFormat.format(addProductDto.getBornBefore()));

            if (!addProductDto.getBornBefore().before(new Date())) {
                return ResponseService.generateErrorResponse("BORN BEFORE DATE MUST BE OF PAST", HttpStatus.BAD_REQUEST);
            } else if (!ref.getBornAfter().before(addProductDto.getBornBefore())) {
                return ResponseService.generateErrorResponse("BORN AFTER DATE MUST BE PAST OF BORN BEFORE DATE", HttpStatus.BAD_REQUEST);
            }

            ref.setBornBefore(addProductDto.getBornBefore());
            return null;

        } catch (ParseException e) {
            return ResponseService.generateErrorResponse("DATE PARSING ERROR: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<?> validateAndUpdateFee(AddProductDto addProductDto, CustomProductReserveCategoryFeePostRef ref) {
        if (addProductDto.getFee() <= 0) {
            return ResponseService.generateErrorResponse(FEELESSTHANOREQUALZERO, HttpStatus.BAD_REQUEST);
        }
        ref.setFee(addProductDto.getFee());
        return null;
    }

    private ResponseEntity<?> validateAndUpdatePost(AddProductDto addProductDto, CustomProductReserveCategoryFeePostRef ref) {
        if (addProductDto.getPost() <= 0) {
            return ResponseService.generateErrorResponse(POSTLESSTHANORZERO, HttpStatus.BAD_REQUEST);
        }
        ref.setPost(addProductDto.getPost());
        return null;
    }

    private ResponseEntity<?> validateNewReserveCategory(AddProductDto addProductDto) {
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

        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Example DateFormat
            dateFormat.parse(dateFormat.format(addProductDto.getBornAfter()));
            dateFormat.parse(dateFormat.format(addProductDto.getBornBefore()));

            if (!addProductDto.getBornBefore().before(new Date()) || !addProductDto.getBornAfter().before(new Date())) {
                return ResponseService.generateErrorResponse("BORN BEFORE DATE AND BORN AFTER DATE MUST BE OF PAST", HttpStatus.BAD_REQUEST);
            } else if (!addProductDto.getBornAfter().before(addProductDto.getBornBefore())) {
                return ResponseService.generateErrorResponse("BORN AFTER DATE MUST BE PAST OF BORN BEFORE DATE", HttpStatus.BAD_REQUEST);
            }

        } catch (ParseException e) {
            return ResponseService.generateErrorResponse("DATE PARSING ERROR: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return null;
    }*/

}