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
            TypedQuery<ServiceProviderEntity> query = entityManager.createQuery(Constant.GET_ALL_SERVICE_PROVIDERS, ServiceProviderEntity.class);
            List<ServiceProviderEntity> serviceProviderEntityList = query.getResultList();



        } catch (Exception exception) {

        }
    }
}
