package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.AddPhysicalRequirementDto;
import com.community.api.entity.CustomGender;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomProductGenderPhysicalRequirementRef;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductGenderPhysicalRequirementService {

    private final ExceptionHandlingService exceptionHandlingService;
    private final ProductService productService;
    private final GenderService genderService;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    public ProductGenderPhysicalRequirementService(ExceptionHandlingService exceptionHandlingService, ProductService productService, GenderService genderService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.productService = productService;
        this.genderService = genderService;
    }

    public List<CustomProductGenderPhysicalRequirementRef> getProductGenderPhysicalRequirementByProductId(Long productId) {
        try {
            CustomProduct customProduct = productService.getCustomProductByCustomProductId(productId);

            Query query = entityManager.createQuery(Constant.GET_PRODUCT_GENDER_PHYSICAL_REQUIREMENT, CustomProductGenderPhysicalRequirementRef.class);
            query.setParameter("customProduct", customProduct);
            List<CustomProductGenderPhysicalRequirementRef> productReserveCategoryBornBeforeAfterRefList = query.getResultList();

            return productReserveCategoryBornBeforeAfterRefList;

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    @Transactional
    public void savePhysicalRequirement(List<AddPhysicalRequirementDto> addPhysicalRequirementDtoList, Product product) {
        try {
            for (AddPhysicalRequirementDto addPhysicalRequirementDto : addPhysicalRequirementDtoList) {

                CustomGender customGender = genderService.getGenderByGenderId(addPhysicalRequirementDto.getGenderId());

                Double height = addPhysicalRequirementDto.getHeight();
                Double weight = addPhysicalRequirementDto.getWeight();
                Double shoeSize = addPhysicalRequirementDto.getShoeSize();
                Double waistSize = addPhysicalRequirementDto.getWaistSize();
                Double chestSize = addPhysicalRequirementDto.getChestSize();

                StringBuilder sql = new StringBuilder("INSERT INTO custom_product_gender_physical_requirement_reference (product_id, gender_id, height, weight");
                List<Object> parameters = new ArrayList<>();

                // Mandatory fields
                parameters.add(product.getId());
                parameters.add(customGender.getGenderId());
                parameters.add(height);
                parameters.add(weight);

                if (shoeSize != null) {
                    sql.append(", shoe_size");
                    parameters.add(shoeSize);
                }

                if (waistSize != null) {
                    sql.append(", waist_size");
                    parameters.add(waistSize);
                }

                if (chestSize != null) {
                    sql.append(", chest_size");
                    parameters.add(chestSize);
                }

                // Complete the query
                sql.append(") VALUES (?, ?, ?, ?");

                // Add placeholders for optional parameters
                for (int i = 4; i < parameters.size(); i++) {
                    sql.append(", ?");
                }
                // Close the statement
                sql.append(")");

                // Create and set parameters in the query
                Query query = entityManager.createNativeQuery(sql.toString());
                query.setParameter(1, product.getId());
                query.setParameter(2, customGender.getGenderId());
                query.setParameter(3, height);
                query.setParameter(4, weight);

                for (int i = 0; i < parameters.size() - 4; i++) {
                    query.setParameter(i + 5, parameters.get(i + 4));
                }

//                Query query = entityManager.createNativeQuery(Constant.ADD_PRODUCT_GENDER_PHYSICAL_REQUIREMENT);
//
//                query.setParameter("productId", product.getId());
//                query.setParameter("genderId", customGender.getGenderId());
//                query.setParameter("height", height);
//                query.setParameter("weight", weight);
//                query.setParameter("shoeSize", shoeSize);
//                query.setParameter("waistSize", waistSize);
//                query.setParameter("chestSize", chestSize);

                int affectedRows = query.executeUpdate();

                if (affectedRows == 0) {
                    throw new RuntimeException("ERROR INSERTING VALUES IN MAPPING TABLE OF CUSTOMPRODUCTGENDERPHYSICALREQUIREMENTREF");
                }
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
        }
    }

    public CustomProductGenderPhysicalRequirementRef getCustomProductGenderPhysicalRequirementRefByProductIdAndGenderId(Long productId, Long genderId) {

        try {
            CustomProduct customProduct = productService.getCustomProductByCustomProductId(productId);
            CustomGender customGender = genderService.getGenderByGenderId(genderId);

            List<CustomProductGenderPhysicalRequirementRef> customProductGenderPhysicalRequirementRefList = entityManager.createQuery("SELECT c FROM CustomProductGenderPhysicalRequirementRef c WHERE c.customProduct = :customProduct AND c.customGender = :customGender", CustomProductGenderPhysicalRequirementRef.class)
                    .setParameter("customProduct", customProduct)
                    .setParameter("customGender", customGender)
                    .getResultList();

            return customProductGenderPhysicalRequirementRefList.get(0);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }

    }

    public boolean removeProductGenderPhysicalRequirementByProductId (CustomProduct customProduct) throws Exception {
        try {

            int rowsAffected = entityManager.createQuery(
                            "DELETE FROM CustomProductGenderPhysicalRequirementRef c WHERE c.customProduct = :customProduct")
                    .setParameter("customProduct", customProduct)
                    .executeUpdate();

            return rowsAffected > 0;

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: " + exception.getMessage());
        }
    }
}
