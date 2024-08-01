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
public class CustomProductService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private Validator validator; // Autowire Validator bean if needed

    public void saveCustomProduct(Date goLiveDate, Integer priorityLevel, Long productId) {
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
    protected Errors validatePriorityLevel(Integer priorityLevel) {
        CustomProduct product = new CustomProduct();
        product.setPriorityLevel(priorityLevel);

        Errors errors = new BeanPropertyBindingResult(product, "customProduct");
        validator.validate(product, errors);
        return errors;
    }

    public List<CustomProduct> getCustomProducts() {
        String sql = "SELECT * FROM ext_product";

        return entityManager.createNativeQuery(sql, CustomProduct.class).getResultList();
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

}