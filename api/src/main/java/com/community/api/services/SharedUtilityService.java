package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.Skill;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Map<String,Object> createProductResponseMap(Product product)
    {
        Map<String, Object> productDetails = new HashMap<>();
        CustomProduct customProduct=entityManager.find(CustomProduct.class,product.getId());
        productDetails.put("product_id", product.getId());
        productDetails.put("url", product.getUrl());
        productDetails.put("url_key", product.getUrlKey());
        productDetails.put("platform_fee",customProduct.getPlatformFee());
        productDetails.put("display_template", product.getDisplayTemplate());
        productDetails.put("default_sku_id", product.getDefaultSku().getId());
        productDetails.put("default_sku_external_id", product.getDefaultSku().getExternalId());
        productDetails.put("default_sku_url_key", product.getDefaultSku().getUrlKey());
        productDetails.put("default_sku_display_template", product.getDefaultSku().getDisplayTemplate());
        productDetails.put("default_sku_cost_amount", product.getDefaultSku().getCost().getAmount());
        productDetails.put("default_sku_cost_currency", product.getDefaultSku().getCost().getCurrency());
        productDetails.put("default_sku_name", product.getDefaultSku().getName());
        productDetails.put("sku_description", product.getDefaultSku().getDescription());
        productDetails.put("description", product.getDefaultSku().getLongDescription());
        productDetails.put("active_start_date", product.getDefaultSku().getActiveStartDate());
        productDetails.put("cost",product.getDefaultSku().getCost());
        productDetails.put("category_id",product.getCategory().getId());
        productDetails.put("active_end_date", product.getDefaultSku().getActiveEndDate());
        return productDetails;
    }
}

