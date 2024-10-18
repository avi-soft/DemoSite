package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Service
public class TicketService {
    @Autowired
    EntityManager entityManager;

    public void randomBindingTicketAllocation() {
        try {
            // first will fetch all the service provider that are currently present in the system.
            TypedQuery<ServiceProviderEntity> query = entityManager.createQuery(Constant.GET_ALL_SERVICE_PROVIDERS, ServiceProviderEntity.class);
            List<ServiceProviderEntity> serviceProviderEntityList = query.getResultList();

            // secondly we will fetch all the orders that are not assigned to any sp yet or returned by a service provider



            // now the idea is that we will traverse the orders one by one and if their referrer is not busy to take their ticket we will assign to that SP them and remove from order list
            // and update its entry in SPTicket service_provider_id, along with ticket_id (This is a imp table which stores the data about Sp and ticket allocated to them)


            // after that all the remaining tickets left by randomBindingTicketAllocation will be forwarded to verticalDistributionTicketAllocation for allocation.
        } catch (Exception exception) {

        }
    }

    // Second choice for binded tickets and first choice for unbinded tickets.
    public void verticalDistributionTicketAllocation() {
        try {
            // firstly we will get the remaining orders from the ticketAllocation method after randomBindingTicketAllocation

            // we will divide the ServiceProvider w.r.t the SP Rank and starts allocating tickets based on the SP Rank starting from Professional to Individual SP.




            
        } catch (Exception exception) {

        }
    }
}
