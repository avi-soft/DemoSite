package com.community.api.services;
import com.community.api.component.Constant;
import com.community.api.entity.CustomOrderState;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;

@Service
public class DummyAssignerService {
    @Autowired
    private OrderService orderService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private OrderStatusByStateService orderStatusByStateService;

        @Transactional
        @Scheduled(fixedRate = 120000) // 120000 milliseconds = 2 minutes
        public void autoAssignOrders() {
            try {
                System.out.println("Auto Assigner Scheduled :");
                Query query = entityManager.createNativeQuery(Constant.GET_NEW_ORDERS);
                List<BigInteger> orderIds = query.getResultList();
                if (orderIds.isEmpty()) {
                    System.out.println("No Orders to assign");
                    return;
                }
                for (BigInteger id : orderIds) {
                    Order order = orderService.findOrderById(id.longValue());
                    if (order != null) {
                        dummyAssigner(order);
                    }
                }
                System.out.println("Orders assigned");
            } catch (Exception e) {
                // Handle the exception appropriately
                exceptionHandling.handleException(e);
                System.err.println("Error Auto Assigning: " + e.getMessage());
            }
        }
        public void dummyAssigner(Order order) {
            Random random = new Random();
            CustomOrderState orderState=entityManager.find(CustomOrderState.class,order.getId());
            int randomNumber = random.nextInt(2);
            if (randomNumber == 1) {
                if (orderState.getOrderStateId().equals(Constant.ORDER_STATE_RETURNED.getOrderStateId())) {
                    orderState.setOrderStateId(Constant.ORDER_STATE_ASSIGNED.getOrderStateId());
                    Integer orderStatusId = orderStatusByStateService.getOrderStatusByOrderStateId(Constant.ORDER_STATE_ASSIGNED.getOrderStateId()).get(1).getOrderStatusId();
                    orderState.setOrderStatusId(orderStatusId);
                } else {
                    order.setStatus(Constant.ORDER_STATUS_AUTO_ASSIGNED);
                    orderState.setOrderStateId(Constant.ORDER_STATE_AUTO_ASSIGNED.getOrderStateId());
                    Integer orderStatusId = orderStatusByStateService.getOrderStatusByOrderStateId(Constant.ORDER_STATE_AUTO_ASSIGNED.getOrderStateId()).get(0).getOrderStatusId();
                    orderState.setOrderStatusId(orderStatusId);
                }
            } else {
                order.setStatus(Constant.ORDER_STATUS_UNASSIGNED);
                orderState.setOrderStateId(Constant.ORDER_STATE_UNASSIGNED.getOrderStateId());
                Integer orderStatusId=orderStatusByStateService.getOrderStatusByOrderStateId(Constant.ORDER_STATE_UNASSIGNED.getOrderStateId()).get(0).getOrderStatusId();
                orderState.setOrderStatusId(orderStatusId);
            }
            entityManager.merge(orderState);
            entityManager.merge(order);
        }
    }


