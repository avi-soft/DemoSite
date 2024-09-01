package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomProductReserveCategoryBornBeforeAfterRef;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

@Service
public class ProductReserveCategoryBornBeforeAfterRefService {
    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomProductReserveCategoryBornBeforeAfterRef> getProductReserveCategoryBornBeforeAfterByProductId(Long productId){
        try{

            Query query = entityManager.createQuery(Constant.GET_PRODUCT_RESERVECATEGORY_BORNBEFORE_BORNAFTER, CustomProductReserveCategoryBornBeforeAfterRef.class);
            query.setParameter("productId", productId);
            List<CustomProductReserveCategoryBornBeforeAfterRef> productReserveCategoryBornBeforeAfterRefList = query.getResultList();

            return productReserveCategoryBornBeforeAfterRefList;

        } catch(Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    public void saveBornBeforeAndBornAfter(Date bornBefore, Date bornAfter, Product product, CustomReserveCategory reserveCategory) {
        try{

            Query query = entityManager.createNativeQuery(Constant.ADD_PRODUCT_RESERVECATEOGRY_BORNBEFORE_BORNAFTER);
            query.setParameter("productId", product);
            query.setParameter("reserveCategoryId", reserveCategory);
            query.setParameter("bornAfter", bornAfter);
            query.setParameter("bornBefore", bornBefore);
            int affectedRows = query.executeUpdate();

            if(affectedRows == 0){
                throw new RuntimeException("Error inserting values in mapping table of CustomProductReserveCategoryBornBeforeAfterRef");
            }

        } catch(Exception exception) {
            exceptionHandlingService.handleException(exception);
        }
    }
}
