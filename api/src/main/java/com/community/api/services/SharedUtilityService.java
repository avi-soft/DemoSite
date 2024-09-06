package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.Skill;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.SanitizableData;
import org.springframework.boot.actuate.endpoint.Sanitizer;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SharedUtilityService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private  ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;
    public long findCount(String queryString) {
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult();
    }
    public static String getCurrentTimestamp() {
        // Get the current date and time with timezone
        ZonedDateTime zonedDateTime = ZonedDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSXXX");
        return zonedDateTime.format(formatter);
    }
    public Map<String,Object> createProductResponseMap(Product product, OrderItem orderItem)
    {
        Map<String, Object> productDetails = new HashMap<>();
        CustomProduct customProduct=entityManager.find(CustomProduct.class,product.getId());
        if(orderItem!=null)
            productDetails.put("order_item_id",orderItem.getId());
        productDetails.put("product_id", product.getId());
        productDetails.put("url", product.getUrl());
        productDetails.put("url_key", product.getUrlKey());
        productDetails.put("platform_fee",customProduct.getPlatformFee());
        productDetails.put("display_template", product.getDisplayTemplate());
        productDetails.put("default_sku_id", product.getDefaultSku().getId());
        productDetails.put("default_sku_name", product.getDefaultSku().getName());
        productDetails.put("sku_description", product.getDefaultSku().getDescription());
        productDetails.put("long_description", product.getDefaultSku().getLongDescription());
        productDetails.put("active_start_date", product.getDefaultSku().getActiveStartDate());//@TODO-Fee is dependent on category
        productDetails.put("fee",productReserveCategoryFeePostRefService.getCustomProductReserveCategoryFeePostRefByProductIdAndReserveCategoryId(product.getId(),1L).getFee());//this is dummy data
        productDetails.put("category_id",product.getCategory().getId());
        productDetails.put("active_end_date", product.getDefaultSku().getActiveEndDate());
        return productDetails;
    }
    public enum ValidationResult {
        SUCCESS,
        EXCEEDS_MAX_SIZE,
        EXCEEDS_NESTED_SIZE,
        INVALID_TYPE
    }
    public ValidationResult validateInputMap(Map<String,Object>inputMap)
    {
            if(inputMap.keySet().size()>Constant.MAX_REQUEST_SIZE)
                return ValidationResult.EXCEEDS_MAX_SIZE;

            // Iterate through the map entries to check for nested maps
            for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
                Object value = entry.getValue();

                // Check if the value is a nested map
                if (value instanceof Map) {
                    Map<?, ?> nestedMap = (Map<?, ?>) value;

                    // Check the size of the nested map's key set
                    if (nestedMap.keySet().size() > Constant.MAX_NESTED_KEY_SIZE) {
                        return ValidationResult.EXCEEDS_NESTED_SIZE;
                    }
                }
            }
            return ValidationResult.SUCCESS;

        }


}

