package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.AddReserveCategoryDto;
import com.community.api.entity.CustomProduct;
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

    private final ExceptionHandlingService exceptionHandlingService;
    private final ProductService productService;
    private final ReserveCategoryService reserveCategoryService;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    public ProductReserveCategoryBornBeforeAfterRefService(ExceptionHandlingService exceptionHandlingService, ProductService productService, ReserveCategoryService reserveCategoryService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.productService = productService;
        this.reserveCategoryService = reserveCategoryService;
    }

    public List<CustomProductReserveCategoryBornBeforeAfterRef> getProductReserveCategoryBornBeforeAfterByProductId(Long productId) {
        try {
            CustomProduct customProduct = productService.getCustomProductByCustomProductId(productId);

            Query query = entityManager.createQuery(Constant.GET_PRODUCT_RESERVECATEGORY_BORNBEFORE_BORNAFTER, CustomProductReserveCategoryBornBeforeAfterRef.class);
            query.setParameter("customProduct", customProduct);
            List<CustomProductReserveCategoryBornBeforeAfterRef> productReserveCategoryBornBeforeAfterRefList = query.getResultList();

            return productReserveCategoryBornBeforeAfterRefList;

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    public void saveBornBeforeAndBornAfter(List<AddReserveCategoryDto> addReserveCategoryDtoList, Product product) {
        try {
            for (AddReserveCategoryDto addReserveCategoryDto : addReserveCategoryDtoList) {

                CustomReserveCategory reserveCategory = reserveCategoryService.getReserveCategoryById(addReserveCategoryDto.getReserveCategory());
                Date bornAfter = addReserveCategoryDto.getBornAfter();
                Date bornBefore = addReserveCategoryDto.getBornBefore();

                Query query = entityManager.createNativeQuery(Constant.ADD_PRODUCT_RESERVECATEOGRY_BORNBEFORE_BORNAFTER);
                query.setParameter("productId", product.getId());
                query.setParameter("reserveCategoryId", reserveCategory.getReserveCategoryId());
                query.setParameter("bornAfter", bornAfter);
                query.setParameter("bornBefore", bornBefore);

                int affectedRows = query.executeUpdate();

                if (affectedRows == 0) {
                    throw new RuntimeException("Error inserting values in mapping table of CustomProductReserveCategoryBornBeforeAfterRef");
                }
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
        }
    }

    public CustomProductReserveCategoryBornBeforeAfterRef getCustomProductReserveCategoryBornBeforeAfterRefByProductIdAndReserveCategoryId(Long productId, Long reserveCategoryId) {

        try {
            CustomProduct customProduct = productService.getCustomProductByCustomProductId(productId);
            CustomReserveCategory customReserveCategory = reserveCategoryService.getReserveCategoryById(reserveCategoryId);

            List<CustomProductReserveCategoryBornBeforeAfterRef> customProductReserveCategoryBornBeforeAfterRefList = entityManager.createQuery("SELECT c FROM CustomProductReserveCategoryBornBeforeAfterRef c WHERE c.customProduct = :customProduct AND c.customReserveCategory = :customReserveCategory", CustomProductReserveCategoryBornBeforeAfterRef.class)
                    .setParameter("customProduct", customProduct)
                    .setParameter("customReserveCategory", customReserveCategory)
                    .getResultList();

            return customProductReserveCategoryBornBeforeAfterRefList.get(0);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }

    }

    public boolean removeProductReserveCategoryBornBeforeAfterByProductId (CustomProduct customProduct) throws Exception {
        try {

            int rowsAffected = entityManager.createQuery(
                            "DELETE FROM CustomProductReserveCategoryBornBeforeAfterRef c WHERE c.customProduct = :customProduct")
                    .setParameter("customProduct", customProduct)
                    .executeUpdate();

            return rowsAffected > 0;

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }
}
