package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class ReserveCategoryService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomReserveCategory> getAllReserveCategory(){
        try{
            List<CustomReserveCategory> reserveCategories = entityManager.createNativeQuery(Constant.GET_ALL_RESERVED_CATEGORY, CustomReserveCategory.class).getResultList();
            return reserveCategories;
        } catch(Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
}
