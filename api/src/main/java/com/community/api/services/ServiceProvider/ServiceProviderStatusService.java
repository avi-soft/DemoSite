package com.community.api.services.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.entity.Role;
import com.community.api.services.ResponseService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Service
public class ServiceProviderStatusService {

    private EntityManager entityManager;

    private ExceptionHandlingImplement exceptionHandling;

    private SharedUtilityService sharedUtilityService;

    private ResponseService responseService;

    public ServiceProviderStatusService(EntityManager entityManager,ExceptionHandlingImplement exceptionHandling,SharedUtilityService sharedUtilityService,ResponseService responseService)
    {
        this.entityManager = entityManager;
        this.exceptionHandling = exceptionHandling;
        this.sharedUtilityService=sharedUtilityService;
        this.responseService=responseService;
    }

    @Transactional
    public ResponseEntity<?> addStatus(ServiceProviderStatus serviceProviderStatus) {
        try {
            if(serviceProviderStatus.getStatus_name()==null||serviceProviderStatus.getDescription()==null)
                return responseService.generateErrorResponse("Empty status name or description",HttpStatus.BAD_REQUEST);
            int count=(int)sharedUtilityService.findCount(Constant.GET_COUNT_OF_STATUS);
            serviceProviderStatus.setStatus_id(++count);
            serviceProviderStatus.setCreated_at(sharedUtilityService.getCurrentTimestamp());
            serviceProviderStatus.setUpdated_at(sharedUtilityService.getCurrentTimestamp());
            serviceProviderStatus.setCreated_by("SUPER_ADMIN");//@TODO-need to fetch created_by from token based on role
            entityManager.persist(serviceProviderStatus);
           return responseService.generateSuccessResponse("Status added successfully",serviceProviderStatus,HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Error Creating status: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public List<ServiceProviderStatus> findAllStatusList() {
        TypedQuery<ServiceProviderStatus> query = entityManager.createQuery(Constant.GET_ALL_STATUS, ServiceProviderStatus.class);
        return query.getResultList();
    }
}
