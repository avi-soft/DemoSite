package com.community.api.endpoint.avisoft.controller.ServiceProvider;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/service-providers")
public class ServiceProviderController {

    @Autowired
    private ServiceProviderServiceImpl serviceProviderService;

    @PostMapping
    public ResponseEntity<ServiceProviderEntity> createServiceProvider(@RequestBody ServiceProviderEntity serviceProviderEntity) throws Exception {
        ServiceProviderEntity savedServiceProvider = serviceProviderService.saveServiceProvider(serviceProviderEntity);

        if (savedServiceProvider == null) {
            throw new Exception("Service provider could not be created");
        }
        return ResponseEntity.ok(savedServiceProvider);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateServiceProvider(@PathVariable Long userId, @Valid @RequestBody ServiceProviderEntity serviceProviderEntity) throws Exception {


        ServiceProviderEntity updatedServiceProvider = serviceProviderService.updateServiceProvider(userId, serviceProviderEntity);
        return ResponseEntity.ok(updatedServiceProvider);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ServiceProviderEntity> getServiceProviderById(@PathVariable Long userId) throws Exception {
        ServiceProviderEntity serviceProviderEntity = serviceProviderService.getServiceProviderById(userId);
        if (serviceProviderEntity == null) {
            throw new Exception("ServiceProvider with ID " + userId + " not found");
        }
        return ResponseEntity.ok(serviceProviderEntity);
    }
}