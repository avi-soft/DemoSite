package com.community.api.endpoint.avisoft.custom;

import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Date;
import java.sql.Timestamp;
import java.util.List;

@Service
public class ExtProductService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void saveExtProduct(Date goLiveDate, Long productId) {
        String sql = "INSERT INTO ext_product (golivedate, product_id) VALUES (:goLiveDate, :productId)";

        entityManager.createNativeQuery(sql)
                .setParameter("goLiveDate", goLiveDate != null ? new Timestamp(goLiveDate.getTime()) : null)
                .setParameter("productId", productId)
                .executeUpdate();
    }

    @Transactional
    public List<CustomProduct> getExtProduct() {
        String sql = "SELECT * FROM ext_product";

        List<CustomProduct> extProducts = entityManager.createNativeQuery(sql, CustomProduct.class).getResultList();
        return extProducts;
    }
}