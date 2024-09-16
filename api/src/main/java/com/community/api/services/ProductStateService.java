package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomProductState;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
public class ProductStateService {
    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomProductState> getAllProductState(){
        try{
            List<CustomProductState> productStateList = entityManager.createQuery(Constant.GET_ALL_PRODUCT_STATE, CustomProductState.class).getResultList();
            return productStateList;
        } catch(Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    public CustomProductState getProductStateById(Long productStateId){
        try{

            Query query = entityManager.createQuery(Constant.GET_PRODUCT_STATE_BY_ID, CustomProductState.class);
            query.setParameter("productStateId", productStateId);
            List<CustomProductState> productState = query.getResultList();

            if (!productState.isEmpty()) {
                return productState.get(0);
            } else {
                return null;
            }

        } catch(Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    public CustomProductState getProductStateByName(String productStateName){
        try{

            Query query = entityManager.createQuery(Constant.GET_PRODUCT_STATE_BY_NAME, CustomProductState.class);
            query.setParameter("productStateName", productStateName);
            List<CustomProductState> productState = query.getResultList();

            if (!productState.isEmpty()) {
                return productState.get(0);
            } else {
                return null;
            }

        } catch(Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

}
