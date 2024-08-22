package com.community.api.endpoint.avisoft.controller.ServiceProvider;

import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.entity.Skill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@RestController
@RequestMapping("/service-providers-status")
public class ServiceProviderStatusController {
    @Autowired
    EntityManager entityManager;
    @Autowired
    private com.community.api.services.ServiceProvider.ServiceProviderStatusService serviceProviderStatusService;


    @PostMapping("/add-status")
    public ResponseEntity<?> addStatus(@RequestBody ServiceProviderStatus serviceProviderStatus) {
        return serviceProviderStatusService.addStatus(serviceProviderStatus);
    }
}
