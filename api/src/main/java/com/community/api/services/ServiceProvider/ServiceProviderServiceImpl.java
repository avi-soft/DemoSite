package com.community.api.services.ServiceProvider;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.lang.reflect.Field;

@Service
public class ServiceProviderServiceImpl implements ServiceProviderService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public ServiceProviderEntity saveServiceProvider(ServiceProviderEntity serviceProviderEntity) {
        try {
            entityManager.persist(serviceProviderEntity);
            ServiceProviderStatus serviceProviderStatus=entityManager.find(ServiceProviderStatus.class,1);
            serviceProviderEntity.setStatus(serviceProviderStatus);
            return serviceProviderEntity;
        } catch (Exception e) {
            throw new RuntimeException("Error saving service provider entity", e);
        }
    }

    @Override
    @Transactional
    public ServiceProviderEntity updateServiceProvider(Long userId, @RequestBody ServiceProviderEntity serviceProviderEntity) throws Exception {
        ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, userId);
        if (existingServiceProvider == null) {
            throw new Exception("ServiceProvider with ID " + userId + " not found");
        }

        serviceProviderEntity.setPrimaryMobileNumber(existingServiceProvider.getPrimaryMobileNumber());
        for(Field field:ServiceProviderEntity.class.getDeclaredFields())
        {
            field.setAccessible(true);
            Object newValue=field.get(serviceProviderEntity);
            if(newValue!=null)
            {
                field.set(existingServiceProvider,newValue);
            }
        }

        entityManager.merge(existingServiceProvider);
        return existingServiceProvider;
    }

    @Override
    public ServiceProviderEntity getServiceProviderById(Long userId) {
        return entityManager.find(ServiceProviderEntity.class, userId);
    }
}