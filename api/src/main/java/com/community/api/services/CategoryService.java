package com.community.api.services;

import org.broadleafcommerce.core.catalog.domain.CategoryProductXref;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.util.List;

@Service
public class CategoryService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<BigInteger> getAllProductsByCategoryId(Long categoryId) {
        String sql = "SELECT product_id FROM blc_category_product_xref WHERE category_id = :categoryId";

        try {
            return entityManager.createNativeQuery(sql).setParameter("categoryId", categoryId).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get Category_Product: " + e.getMessage(), e);
        }
    }
}
