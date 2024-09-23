package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.UpdateTestStatusRank;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.Qualification;
import com.community.api.entity.ServiceProviderRank;
import com.community.api.entity.ServiceProviderTestStatus;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

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
    private QualificationService qualificationService;
    @Autowired
    private ResponseService responseService;

    public List<ServiceProviderTestStatus> getAllTestStatus() {
        TypedQuery<ServiceProviderTestStatus> query = entityManager.createQuery(Constant.FIND_ALL_SERVICE_PROVIDER_TEST_STATUS_QUERY, ServiceProviderTestStatus.class);
        List<ServiceProviderTestStatus> serviceProviderTestStatusList = query.getResultList();
        return serviceProviderTestStatusList;
    }

    @Transactional
    public ResponseEntity<?> updateTestStatusRank(UpdateTestStatusRank updateTestStatusRank, Long serviceProviderId)
    {
        try
        {
            ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (existingServiceProvider == null) {
                return responseService.generateErrorResponse("Service Provider Not found", HttpStatus.NOT_FOUND);
            }
            if(updateTestStatusRank.getTest_status_id()!=null)
            {
                ServiceProviderTestStatus serviceProviderTestStatus= entityManager.find(ServiceProviderTestStatus.class,updateTestStatusRank.getTest_status_id());
                if(serviceProviderTestStatus==null)
                {
                    return responseService.generateErrorResponse("Test Status id "+ updateTestStatusRank.getTest_status_id()+" Not found", HttpStatus.NOT_FOUND);
                }
                if (Objects.nonNull(updateTestStatusRank.getTest_status_id())) {
                    existingServiceProvider.setTestStatus(serviceProviderTestStatus);
                }
            }
            if(updateTestStatusRank.getRank_id()!=null)
            {
                ServiceProviderRank serviceProviderRank = entityManager.find(ServiceProviderRank.class,updateTestStatusRank.getRank_id());
                if(serviceProviderRank ==null)
                {
                    return responseService.generateErrorResponse("Rank id "+ updateTestStatusRank.getTest_status_id()+" Not found", HttpStatus.NOT_FOUND);
                }
                if (Objects.nonNull(updateTestStatusRank.getRank_id())) {
                    existingServiceProvider.setRanking(serviceProviderRank);
                }
            }
            entityManager.merge(existingServiceProvider);
            return responseService.generateSuccessResponse("Test Status and rank is updated",existingServiceProvider,HttpStatus.OK);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating test status and rank", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
