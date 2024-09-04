package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductReserveCategoryBornBeforeAfterRef;
import com.community.api.entity.CustomProductReserveCategoryFeePostRef;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

@Service
public class ProductReserveCategoryFeePostRefService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Autowired
    protected ProductService productService;

    @Autowired
    protected  ReserveCategoryService reserveCategoryService;

    public List<CustomProductReserveCategoryFeePostRef> getProductReserveCategoryFeeAndPostByProductId(Long productId){
        try{

            CustomProduct customProduct = productService.getCustomProductByCustomProductId(productId);
            Query query = entityManager.createQuery(Constant.GET_PRODUCT_RESERVECATEGORY_FEE_POST, CustomProductReserveCategoryFeePostRef.class);
            query.setParameter("customProduct", customProduct);
            List<CustomProductReserveCategoryFeePostRef> productReserveCategoryFeePostList = query.getResultList();

            return productReserveCategoryFeePostList;

        } catch(Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    public void saveFeeAndPost(Double fee, Integer post, Product product, CustomReserveCategory reserveCategory) {
        try{

            Query query = entityManager.createNativeQuery(Constant.ADD_PRODUCT_RESERVECATEOGRY_FEE_POST);
            query.setParameter("productId", product);
            query.setParameter("reserveCategoryId", reserveCategory);
            query.setParameter("fee", fee);
            query.setParameter("post", post);
            int affectedRows = query.executeUpdate();

            if(affectedRows == 0){
                throw new RuntimeException("Error inserting values in mapping table of CustomProductReserveCategoryFeePostRef");
            }

        } catch(Exception exception) {
            exceptionHandlingService.handleException(exception);
        }
    }

    public CustomProductReserveCategoryFeePostRef getCustomProductReserveCategoryFeePostRefByProductIdAndReserveCategoryId(Long productId, Long reserveCategoryId) {

        try {
            CustomProduct customProduct = productService.getCustomProductByCustomProductId(productId);
            CustomReserveCategory customReserveCategory = reserveCategoryService.getReserveCategoryById(reserveCategoryId);

            List<CustomProductReserveCategoryFeePostRef> customProductReserveCategoryFeePostRefList = entityManager.createQuery("SELECT c FROM CustomProductReserveCategoryFeePostRef c WHERE c.customProduct = :customProduct AND c.customReserveCategory = :customReserveCategory", CustomProductReserveCategoryFeePostRef.class)
                    .setParameter("customProduct", customProduct)
                    .setParameter("customReserveCategory", customReserveCategory)
                    .getResultList();

            return customProductReserveCategoryFeePostRefList.get(0);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
}
