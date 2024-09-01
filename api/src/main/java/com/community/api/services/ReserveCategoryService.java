package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomProductState;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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

    public CustomReserveCategory getReserveCategoryById(Long reserveCategoryId) {
        try{
            Query query = entityManager.createQuery(Constant.GET_RESERVED_CATEGORY_BY_ID, CustomReserveCategory.class);
            query.setParameter("reserveCategoryId", reserveCategoryId);
            List<CustomReserveCategory> reserveCategory = query.getResultList();

            if (!reserveCategory.isEmpty()) {
                return reserveCategory.get(0);
            } else {
                return null;
            }
        } catch(Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }

    }
}
