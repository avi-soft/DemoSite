package com.community.api.services;
import com.community.api.component.Constant;
import com.community.api.entity.CustomOrderState;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.core.order.service.type.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;

@Service
public class DummyAssigner {
    @Autowired
    private OrderService orderService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

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
                        randomNumberForAssigner(order);
                    }
                }
                System.out.println("Orders assigned");
            } catch (Exception e) {
                // Handle the exception appropriately
                exceptionHandling.handleException(e);
                System.err.println("Error Auto Assigning: " + e.getMessage());
            }
        }

        private void randomNumberForAssigner(Order order) {
            Random random = new Random();
            CustomOrderState orderState=entityManager.find(CustomOrderState.class,order.getId());
            int randomNumber = random.nextInt(2);
            if (randomNumber == 1) {
                order.setStatus(Constant.ORDER_STATUS_AUTO_ASSIGNED);
                orderState.setOrderState(Constant.ORDER_STATE_AUTO_ASSIGNED.getOrderState());
                orderState.setOrderStateDescription(Constant.ORDER_STATE_AUTO_ASSIGNED.getOrderStateDescription());
            } else {
                order.setStatus(Constant.ORDER_STATUS_UNASSIGNED);
                orderState.setOrderState(Constant.ORDER_STATE_UNASSIGNED.getOrderState());
                orderState.setOrderStateDescription(Constant.ORDER_STATE_UNASSIGNED.getOrderStateDescription());
            }
            entityManager.merge(orderState);
            entityManager.merge(order);
        }
    }


