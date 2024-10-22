package com.community.api.services;

import com.community.api.entity.CustomOrderState;
import com.community.api.entity.OrderStateRef;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ServiceProviderTicketService {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderTicketService.class);

    @Autowired
    ServiceProviderServiceImpl serviceProviderService;

    @Autowired
    OrderStateRefService orderStateRefService;

    @Autowired
    CustomOrderService customOrderService;

    public void randomBindingTicketAllocation() {
        try{
            // we are fetching SP who are in approved state (later we will do only active)
            List<Map<String,Object>> serviceProvider = (List<Map<String, Object>>) serviceProviderService.searchServiceProviderBasedOnGivenFields(null,null,null,null,null, 1L);
//            logger.info(serviceProvider.toString());

            OrderStateRef orderStateRef = orderStateRefService.getOrderStateByOrderStateId(1);
            List<CustomOrderState> customOrders = customOrderService.getCustomOrdersByOrderStateId(orderStateRef.getOrderStateId());
            logger.info(customOrders.toString());

        } catch (Exception exception) {
            System.out.println("Exception caught: " + exception.getMessage());
        }
    }

    public void verticalDistributionTicketAllocation() {

    }

}
