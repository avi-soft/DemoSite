package com.community.api.services;

import com.community.api.dto.ReserveCategoryDto;
import com.community.api.entity.CustomProductReserveCategoryBornBeforeAfterRef;
import com.community.api.entity.CustomProductReserveCategoryFeePostRef;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReserveCategoryDtoService {
    private EntityManager entityManager;

    private ExceptionHandlingService exceptionHandlingService;
    private ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService;
    private ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;
    private ReserveCategoryService reserveCategoryService;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setExceptionHandlingService(ExceptionHandlingService exceptionHandlingService) {
        this.exceptionHandlingService = exceptionHandlingService;
    }

    @Autowired
    public void setProductReserveCategoryBornBeforeAfterRefService(ProductReserveCategoryBornBeforeAfterRefService productReserveCategoryBornBeforeAfterRefService) {
        this.productReserveCategoryBornBeforeAfterRefService = productReserveCategoryBornBeforeAfterRefService;
    }

    @Autowired
    public void setProductReserveCategoryFeePostRefService(ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService) {
        this.productReserveCategoryFeePostRefService = productReserveCategoryFeePostRefService;
    }

    @Autowired
    public void setReserveCategoryService(ReserveCategoryService reserveCategoryService) {
        this.reserveCategoryService = reserveCategoryService;
    }

    public List<ReserveCategoryDto> getReserveCategoryDto(Long productId) {
        try{
            List<CustomProductReserveCategoryBornBeforeAfterRef> customProductReserveCategoryBornBeforeAfterRefList = productReserveCategoryBornBeforeAfterRefService.getProductReserveCategoryBornBeforeAfterByProductId(productId);
            List<CustomProductReserveCategoryFeePostRef> customProductReserveCategoryFeePostRefList = productReserveCategoryFeePostRefService.getProductReserveCategoryFeeAndPostByProductId(productId);

            List<ReserveCategoryDto> reserveCategoryDtoList = new ArrayList<>();
            for(int customProductReserveCategoryBornBeforeAfterRefListIndex = 0; customProductReserveCategoryBornBeforeAfterRefListIndex < customProductReserveCategoryBornBeforeAfterRefList.size(); customProductReserveCategoryBornBeforeAfterRefListIndex++) {
                for(int customProductReserveCategoryFeePostRefListIndex = 0; customProductReserveCategoryFeePostRefListIndex < customProductReserveCategoryFeePostRefList.size(); customProductReserveCategoryFeePostRefListIndex++) {
                    if(customProductReserveCategoryBornBeforeAfterRefList.get(customProductReserveCategoryBornBeforeAfterRefListIndex).getCustomReserveCategory().getReserveCategoryId().equals(customProductReserveCategoryFeePostRefList.get(customProductReserveCategoryFeePostRefListIndex).getCustomReserveCategory().getReserveCategoryId()) && customProductReserveCategoryBornBeforeAfterRefList.get(customProductReserveCategoryBornBeforeAfterRefListIndex).getCustomProduct().getId().equals(customProductReserveCategoryFeePostRefList.get(customProductReserveCategoryFeePostRefListIndex).getCustomProduct().getId())) {
                        ReserveCategoryDto reserveCategoryDto = new ReserveCategoryDto();
                        reserveCategoryDto.setProductId(productId);
                        reserveCategoryDto.setReserveCategoryId(customProductReserveCategoryBornBeforeAfterRefList.get(customProductReserveCategoryBornBeforeAfterRefListIndex).getCustomReserveCategory().getReserveCategoryId());
                        reserveCategoryDto.setReserveCategory(reserveCategoryService.getReserveCategoryById(reserveCategoryDto.getReserveCategoryId()).getReserveCategoryName());
                        reserveCategoryDto.setPost(customProductReserveCategoryFeePostRefList.get(customProductReserveCategoryFeePostRefListIndex).getPost());
                        reserveCategoryDto.setFee(customProductReserveCategoryFeePostRefList.get(customProductReserveCategoryFeePostRefListIndex).getFee());
                        reserveCategoryDto.setBornBefore(customProductReserveCategoryBornBeforeAfterRefList.get(customProductReserveCategoryBornBeforeAfterRefListIndex).getBornBefore());
                        reserveCategoryDto.setBornAfter(customProductReserveCategoryBornBeforeAfterRefList.get(customProductReserveCategoryBornBeforeAfterRefListIndex).getBornAfter());

                        reserveCategoryDtoList.add(reserveCategoryDto);
                        break;
                    }
                }
            }
            return reserveCategoryDtoList;

        } catch(Exception exception) {
                exceptionHandlingService.handleException(exception);
                return null;
        }
    }
}
