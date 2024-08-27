package com.community.api.services.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Service
public class ServiceProviderStatusService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    @Transactional
    public ResponseEntity<?> addStatus(ServiceProviderStatus serviceProviderStatus) {
        try {
            if(serviceProviderStatus.getStatus_name()==null)
                return new ResponseEntity<>("Empty status name",HttpStatus.BAD_REQUEST);
            int count=(int)sharedUtilityService.findCount(Constant.GET_COUNT_OF_STATUS);
            serviceProviderStatus.setStatus_id(++count);
            serviceProviderStatus.setCreated_at(sharedUtilityService.getCurrentTimestamp());
            serviceProviderStatus.setUpdated_at(sharedUtilityService.getCurrentTimestamp());
            serviceProviderStatus.setCreated_by("SUPER_ADMIN");//@TODO-need to fetch created_by from token based on role
            entityManager.persist(serviceProviderStatus);
            return new ResponseEntity<>(serviceProviderStatus,HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return new ResponseEntity<>("Error Creating status: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
