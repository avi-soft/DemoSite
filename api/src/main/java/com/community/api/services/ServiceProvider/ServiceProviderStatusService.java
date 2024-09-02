package com.community.api.services.ServiceProvider;

import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
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

    @Transactional
    public ResponseEntity<?> addStatus(ServiceProviderStatus serviceProviderStatus) {
        try {
            if (serviceProviderStatus.getStatusId() == null || serviceProviderStatus.getDescription() == null) {
                return new ResponseEntity<>("Error creating status: Field empty", HttpStatus.BAD_REQUEST);
            } else {
                entityManager.persist(serviceProviderStatus);
                return new ResponseEntity<>(serviceProviderStatus, HttpStatus.OK);
            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return new ResponseEntity<>("Error Creating status: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
