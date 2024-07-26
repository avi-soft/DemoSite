package com.community.api.endpoint.avisoft.custom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Date;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
public class ExtProductService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private Validator validator; // Autowire Validator bean if needed

    public void saveExtProduct(Date goLiveDate, Integer priorityLevel, Long productId) {
        // Validate priorityLevel
        Errors errors = validatePriorityLevel(priorityLevel);
        if (errors.hasErrors()) {
            throw new IllegalArgumentException("Validation error: " + errors.getFieldError().getDefaultMessage());
        }

        String sql = "INSERT INTO ext_product (goliveDate, prioritylevel, product_id) VALUES (:goLiveDate, :priorityLevel, :productId)";

        try {
            entityManager.createNativeQuery(sql)
                    .setParameter("goLiveDate", goLiveDate != null ? new Timestamp(goLiveDate.getTime()) : null)
                    .setParameter("priorityLevel", priorityLevel)
                    .setParameter("productId", productId)
                    .executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save Ext Product: " + e.getMessage(), e);
        }
    }

    /*
       Validate priority level.

       @param priorityLevel The priority level to validate.
       @return Errors object containing validation errors, if any.
     */
    private Errors validatePriorityLevel(Integer priorityLevel) {
        CustomProduct product = new CustomProduct();
        product.setPriorityLevel(priorityLevel);

        Errors errors = new BeanPropertyBindingResult(product, "customProduct");
        validator.validate(product, errors);
        return errors;
    }

    @Transactional
    public List<CustomProduct> getExtProducts() {
        String sql = "SELECT * FROM ext_product";

        return entityManager.createNativeQuery(sql, CustomProduct.class).getResultList();
    }

    /*@PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void saveExtProduct(Date goLiveDate, Integer priorityLevel, Long productId) {
        String sql = "INSERT INTO ext_product (golivedate, priorityLevel, product_id) VALUES (:goLiveDate, :priorityLevel, :productId)";

        entityManager.createNativeQuery(sql)
                .setParameter("goLiveDate", goLiveDate != null ? new Timestamp(goLiveDate.getTime()) : null)
                .setParameter("priorityLevel", priorityLevel)
                .setParameter("productId", productId)
                .executeUpdate();
    }

    @Transactional
    public List<CustomProduct> getExtProduct() {
        String sql = "SELECT * FROM ext_product";

        List<CustomProduct> extProducts = entityManager.createNativeQuery(sql, CustomProduct.class).getResultList();
        return extProducts;
    }*/
}