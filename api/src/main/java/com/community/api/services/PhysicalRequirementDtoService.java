package com.community.api.services;

import com.community.api.dto.PhysicalRequirementDto;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.entity.CustomProductGenderPhysicalRequirementRef;
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
public class PhysicalRequirementDtoService {
    private EntityManager entityManager;

    private ExceptionHandlingService exceptionHandlingService;
    private ProductGenderPhysicalRequirementService productGenderPhysicalRequirementService;
    private GenderService genderService;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setExceptionHandlingService(ExceptionHandlingService exceptionHandlingService) {
        this.exceptionHandlingService = exceptionHandlingService;
    }

    @Autowired
    public void setProductGenderPhysicalRequirementService(ProductGenderPhysicalRequirementService productGenderPhysicalRequirementService) {
        this.productGenderPhysicalRequirementService = productGenderPhysicalRequirementService;
    }

    @Autowired
    public void setGenderService(GenderService genderService) {
        this.genderService = genderService;
    }

    public List<PhysicalRequirementDto> getPhysicalRequirementDto(Long productId) {
        try{
            List<CustomProductGenderPhysicalRequirementRef> customProductGenderPhysicalRequirementRefList = productGenderPhysicalRequirementService.getProductGenderPhysicalRequirementByProductId(productId);

            List<PhysicalRequirementDto> physicalRequirementDtoList = new ArrayList<>();
            for(int customProductGenderPhysicalRequirementRefListIndex = 0; customProductGenderPhysicalRequirementRefListIndex < customProductGenderPhysicalRequirementRefList.size(); customProductGenderPhysicalRequirementRefListIndex++) {

                PhysicalRequirementDto physicalRequirementDto = new PhysicalRequirementDto();
                physicalRequirementDto.setProductId(productId);
                physicalRequirementDto.setGenderId(customProductGenderPhysicalRequirementRefList.get(customProductGenderPhysicalRequirementRefListIndex).getCustomGender().getGenderId());
                physicalRequirementDto.setHeight(customProductGenderPhysicalRequirementRefList.get(customProductGenderPhysicalRequirementRefListIndex).getHeight());
                physicalRequirementDto.setWeight(customProductGenderPhysicalRequirementRefList.get(customProductGenderPhysicalRequirementRefListIndex).getWeight());
                physicalRequirementDto.setChestSize(customProductGenderPhysicalRequirementRefList.get(customProductGenderPhysicalRequirementRefListIndex).getChestSize());
                physicalRequirementDto.setShoeSize(customProductGenderPhysicalRequirementRefList.get(customProductGenderPhysicalRequirementRefListIndex).getShoeSize());
                physicalRequirementDto.setWaistSize(customProductGenderPhysicalRequirementRefList.get(customProductGenderPhysicalRequirementRefListIndex).getWaistSize());

                physicalRequirementDtoList.add(physicalRequirementDto);

            }
            return physicalRequirementDtoList;

        } catch(Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
}
