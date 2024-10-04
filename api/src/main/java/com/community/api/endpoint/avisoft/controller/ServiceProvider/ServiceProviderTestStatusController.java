package com.community.api.endpoint.avisoft.controller.ServiceProvider;

import com.community.api.dto.UpdateTestStatus;
import com.community.api.entity.ServiceProviderTestStatus;
import com.community.api.services.ResponseService;
import com.community.api.services.ServiceProviderTestStatusService;
import com.community.api.services.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

import static com.community.api.component.Constant.FIND_ALL_SERVICE_PROVIDER_TEST_STATUS_QUERY;

@RestController
@RequestMapping("/service-provider-test-status")
public class ServiceProviderTestStatusController {

    private EntityManager entityManager;
    private ResponseService responseService;
    protected ExceptionHandlingImplement exceptionHandling;
    private ServiceProviderTestStatusService serviceProviderTestStatusService;


    public ServiceProviderTestStatusController(EntityManager entityManager, ResponseService responseService, ExceptionHandlingImplement exceptionHandling, ServiceProviderTestStatusService serviceProviderTestStatusService) {
        this.responseService = responseService;
        this.entityManager = entityManager;
        this.exceptionHandling = exceptionHandling;
        this.serviceProviderTestStatusService = serviceProviderTestStatusService;
    }


    @GetMapping("/get-all-service-provider-test-status")

    public ResponseEntity<?> getAllServiceProviderTestStatus() {

        try{
            TypedQuery<ServiceProviderTestStatus> query = entityManager.createQuery(FIND_ALL_SERVICE_PROVIDER_TEST_STATUS_QUERY, ServiceProviderTestStatus.class);
            List<ServiceProviderTestStatus> serviceProviderTestStatusList = query.getResultList();
            if (serviceProviderTestStatusList.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK, "Service Provider Test Status List is Empty", serviceProviderTestStatusList);
            }
            return responseService.generateResponse(HttpStatus.OK, "Service Provider Test Status List Retrieved Successfully", serviceProviderTestStatusList);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error fetching service provider test status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    @PatchMapping("/update-test-status/{serviceProviderId}")
    public ResponseEntity<?> updateTestStatus(@RequestBody UpdateTestStatus updateTestStatus, @PathVariable Long serviceProviderId) {
        try {
            return serviceProviderTestStatusService.updateTestStatus(updateTestStatus,serviceProviderId);
        }  catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error updating: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
