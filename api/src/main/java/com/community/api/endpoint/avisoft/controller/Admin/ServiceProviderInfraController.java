package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.entity.ServiceProviderInfra;
import com.community.api.services.ServiceProviderInfraService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/infra",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class ServiceProviderInfraController {
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private ServiceProviderInfraService serviceProviderInfraService;

    @PostMapping("addInfra")
    public ResponseEntity<?> addInfra(@RequestBody ServiceProviderInfra serviceProviderInfra) {
        try {
            return serviceProviderInfraService.addInfra(serviceProviderInfra);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return new ResponseEntity<>("Error adding infra to list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("fetchInfraList")
    public ResponseEntity<?> fetchInfra() {
        try {
            return new ResponseEntity<>(serviceProviderInfraService.findAllInfraList(), HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return new ResponseEntity<>("Error adding infra to list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
