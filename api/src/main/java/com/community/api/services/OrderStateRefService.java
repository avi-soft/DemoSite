package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.OrderStateRef;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
public class OrderStateRefService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<OrderStateRef> getAllOrderState() {
        try {
            List<OrderStateRef> orderStateList = entityManager.createQuery(Constant.GET_ALL_ORDER_STATE, OrderStateRef.class).getResultList();
            return orderStateList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    public OrderStateRef getOrderStateByOrderStateId(Integer orderStateId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_ORDER_STATE_BY_ORDER_STATE_ID, OrderStateRef.class);
            query.setParameter("orderStateId", orderStateId);
            List<OrderStateRef> orderState = query.getResultList();

            if (!orderState.isEmpty()) {
                return orderState.get(0);
            } else {
                return null;
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    public OrderStateRef getOrderStateByOrderStateName(String orderStateName) {
        try {

            Query query = entityManager.createQuery(Constant.GET_ORDER_STATE_BY_ORDER_STATE_NAME, OrderStateRef.class);
            query.setParameter("orderStateName", orderStateName);
            List<OrderStateRef> orderState = query.getResultList();

            if (!orderState.isEmpty()) {
                return orderState.get(0);
            } else {
                return null;
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
}
