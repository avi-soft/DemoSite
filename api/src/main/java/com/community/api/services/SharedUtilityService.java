package com.community.api.services;

import com.community.api.component.Constant;
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
        productDetails.put("id", product.getId());
        productDetails.put("url", product.getUrl());
        productDetails.put("urlKey", product.getUrlKey());
        productDetails.put("displayTemplate", product.getDisplayTemplate());
        productDetails.put("defaultSkuId", product.getDefaultSku().getId());
        productDetails.put("defaultSkuExternalId", product.getDefaultSku().getExternalId());
        productDetails.put("defaultSkuUrlKey", product.getDefaultSku().getUrlKey());
        productDetails.put("defaultSkuDisplayTemplate", product.getDefaultSku().getDisplayTemplate());
        productDetails.put("defaultSkuSalePrice", product.getDefaultSku().getSalePrice());
        productDetails.put("defaultSkuRetailPrice", product.getDefaultSku().getRetailPrice());
        productDetails.put("defaultSkuCostAmount", product.getDefaultSku().getCost().getAmount());
        productDetails.put("defaultSkuCostCurrency", product.getDefaultSku().getCost().getCurrency());
        productDetails.put("defaultSkuName", product.getDefaultSku().getName());
        productDetails.put("defaultSkuDescription", product.getDefaultSku().getDescription());
        productDetails.put("defaultSkuLongDescription", product.getDefaultSku().getLongDescription());
        productDetails.put("defaultSkuTaxCode", product.getDefaultSku().getTaxCode());
        productDetails.put("defaultSkuTaxable", product.getDefaultSku().isTaxable());
        productDetails.put("defaultSkuDiscountable", product.getDefaultSku().isDiscountable());
        productDetails.put("defaultSkuActiveStartDate", product.getDefaultSku().getActiveStartDate());
        productDetails.put("cost",product.getDefaultSku().getCost());
        productDetails.put("categoryId",product.getCategory().getId());
        productDetails.put("defaultSkuActiveEndDate", product.getDefaultSku().getActiveEndDate());
        return productDetails;
    }
}

