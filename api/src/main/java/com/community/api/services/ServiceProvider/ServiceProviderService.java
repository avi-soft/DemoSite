package com.community.api.services.ServiceProvider;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;

public interface ServiceProviderService {
    ServiceProviderEntity saveServiceProvider(ServiceProviderEntity serviceProviderEntity);
    ServiceProviderEntity updateServiceProvider(Long userId, ServiceProviderEntity serviceProviderEntity) throws Exception;
    ServiceProviderEntity getServiceProviderById(Long userId);
}