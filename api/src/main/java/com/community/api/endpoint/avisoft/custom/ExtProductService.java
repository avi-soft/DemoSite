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
    public void saveExtProduct(Date createdDate, Date expirationDate, Date goLiveDate, Long productId) {
        String sql = "INSERT INTO ext_product (created_date, expiration_date, go_live_date, product_id) VALUES (:createdDate, :expirationDate, :goLiveDate, :productId)";

        entityManager.createNativeQuery(sql)
                .setParameter("createdDate", createdDate != null ? new Timestamp(createdDate.getTime()) : null)
                .setParameter("expirationDate", expirationDate != null ? new Timestamp(expirationDate.getTime()) : null)
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