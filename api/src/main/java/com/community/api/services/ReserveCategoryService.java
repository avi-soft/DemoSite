package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
    public CustomReserveCategory getCategoryByName(String name)
    {
        try
        {
            return entityManager.createQuery(Constant.GET_RESERVE_CATEGORY_BY_ID, CustomReserveCategory.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }catch(Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
    public Double getReserveCategoryFee(Long pid, Long reserveCategoryId) {
        Query query = entityManager.createNativeQuery(Constant.GET_RESERVE_CATEGORY_FEE);
        query.setParameter("pid", pid);
        query.setParameter("reserveCategoryId", reserveCategoryId);

        try {
            return (Double) query.getSingleResult();
        } catch (NoResultException e) {
            return null; // Return null if no result is found
        }
    }
}
