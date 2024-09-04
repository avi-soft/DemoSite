package com.community.api.services;

import com.community.api.dto.AddProductDto;
import com.community.api.entity.*;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.ProductImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.context.annotation.ApplicationScope;

@Service
public class ProductService {

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

}