package com.community.api.endpoint.serviceProvider;

import com.community.api.endpoint.customer.CustomerDTO;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Entity;
import javax.persistence.EntityManager;

@RestController
@RequestMapping("/service-providers-status")
public class StatusEndpoint {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Transactional
    @RequestMapping(value = "add-status", method = RequestMethod.POST)
    public ResponseEntity<?> addStatus(@RequestBody ServiceProviderStatus serviceProviderStatus) {
        try {
            if (serviceProviderStatus.getStatusId() == null || serviceProviderStatus.getDescription() == null) {
                return new ResponseEntity<>("Error creating status :Field empty", HttpStatus.BAD_REQUEST);
            } else
                entityManager.persist(serviceProviderStatus);
            return new ResponseEntity<>(serviceProviderStatus,HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return new ResponseEntity<>("Error Creating status : "+exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}