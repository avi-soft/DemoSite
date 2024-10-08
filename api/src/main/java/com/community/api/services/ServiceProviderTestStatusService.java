package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.UpdateTestStatus;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.ServiceProviderRank;
import com.community.api.entity.ServiceProviderTestStatus;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;

@Service
public class ServiceProviderTestStatusService {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private ResponseService responseService;

    public List<ServiceProviderTestStatus> getAllTestStatus() {
        TypedQuery<ServiceProviderTestStatus> query = entityManager.createQuery(Constant.FIND_ALL_SERVICE_PROVIDER_TEST_STATUS_QUERY, ServiceProviderTestStatus.class);
        List<ServiceProviderTestStatus> serviceProviderTestStatusList = query.getResultList();
        return serviceProviderTestStatusList;
    }

    @Transactional
    public ResponseEntity<?> updateTestStatus(UpdateTestStatus updateTestStatus, Long serviceProviderId)
    {
        try
        {
            ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (existingServiceProvider == null) {
                return responseService.generateErrorResponse("Service Provider Not found", HttpStatus.NOT_FOUND);
            }
            if(updateTestStatus.getTest_status_id()!=null)
            {
                ServiceProviderTestStatus serviceProviderTestStatus= entityManager.find(ServiceProviderTestStatus.class, updateTestStatus.getTest_status_id());
                if(serviceProviderTestStatus==null)
                {
                    return responseService.generateErrorResponse("Test Status id "+ updateTestStatus.getTest_status_id()+" Not found", HttpStatus.NOT_FOUND);
                }
                if (Objects.nonNull(updateTestStatus.getTest_status_id())) {
                    existingServiceProvider.setTestStatus(serviceProviderTestStatus);
                }
            }
            entityManager.merge(existingServiceProvider);
            return responseService.generateSuccessResponse("Test Status is updated",existingServiceProvider,HttpStatus.OK);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating test status", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

