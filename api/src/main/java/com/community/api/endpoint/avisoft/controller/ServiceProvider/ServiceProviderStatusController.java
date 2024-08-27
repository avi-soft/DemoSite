package com.community.api.endpoint.avisoft.controller.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.entity.Role;
import com.community.api.entity.Skill;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping("/serviceProvidersStatus")
public class ServiceProviderStatusController {
    @Autowired
    EntityManager entityManager;
    @Autowired
    private com.community.api.services.ServiceProvider.ServiceProviderStatusService serviceProviderStatusService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @PostMapping("/addStatus")
    public ResponseEntity<?> addStatus(@RequestBody ServiceProviderStatus serviceProviderStatus) {
        return serviceProviderStatusService.addStatus(serviceProviderStatus);
    }
    @GetMapping("/getStatusList")
    public ResponseEntity<?> getStatusList() {
        try{
            return new ResponseEntity<>(serviceProviderStatusService.findAllStatusList(),HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some updating: " + e.getMessage());
        }
    }
}
