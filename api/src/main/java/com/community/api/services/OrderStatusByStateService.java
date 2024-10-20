package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomOrderStatus;
import com.community.api.entity.OrderStateRef;
import com.community.api.services.exception.ExceptionHandlingImplement;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;

@Service
public class OrderStatusByStateService {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    public List<CustomOrderStatus> getOrderStatusByOrderStateId(Integer orderStateId) {
        try {
            Query query = entityManager.createQuery("SELECT s FROM CustomOrderStatus s WHERE s.orderStateId = :orderStateId", CustomOrderStatus.class);
            query.setParameter("orderStateId", orderStateId);
            List<CustomOrderStatus> results = query.getResultList();

            if (results.isEmpty()) {
                throw new NoResultException("No results found");
            }

            return results;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return null;
        }
    }
    public OrderStateRef getOrderStateByOrderStatus(Integer orderStatusId) {
        try {
            Query query = entityManager.createQuery("SELECT s.orderStateId FROM CustomOrderStatus s WHERE s.orderStatusId = :orderStatusId",Integer.class);
            query.setParameter("orderStatusId", orderStatusId);
            Integer id=(Integer)query.getSingleResult();
            OrderStateRef orderStateRef=entityManager.find(OrderStateRef.class,id);
            if(orderStateRef==null)
                throw new NotFoundException("No order state found for this orderStatus id");
            return orderStateRef;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return null;
        }
    }
    public OrderStateRef getOrderStateById(Integer orderStateId) {
        try {
            Query query = entityManager.createQuery("SELECT s.orderStateId FROM OrderStateRef s WHERE s.orderStateId = :orderStateId",Integer.class);
            query.setParameter("orderStateId", orderStateId);
            Integer id=(Integer)query.getSingleResult();
            OrderStateRef orderStateRef=entityManager.find(OrderStateRef.class,id);
            if(orderStateRef==null)
                throw new NotFoundException("No order state found for this orderStatus id");
            return orderStateRef;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return null;
        }
    }
}
