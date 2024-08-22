package com.community.api.services.ServiceProvider;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import org.springframework.http.ResponseEntity;

public interface ServiceProviderService {
    ServiceProviderEntity saveServiceProvider(ServiceProviderEntity serviceProviderEntity);
    ResponseEntity<?> updateServiceProvider(Long userId, ServiceProviderEntity serviceProviderEntity) throws Exception;
    ServiceProviderEntity getServiceProviderById(Long userId);
}