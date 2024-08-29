package com.community.api.endpoint.avisoft.controller.ServiceProvider;

import com.community.api.entity.ServiceProviderTest;
import com.community.api.services.ServiceProviderTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("service-provider-test")
public class ServiceProviderTestController {

    @Autowired
    private ServiceProviderTestService testService;

    @PostMapping("/start/{serviceProviderId}")
    public ResponseEntity<ServiceProviderTest> startTest(@PathVariable Long serviceProviderId) {
        ServiceProviderTest test = testService.startTest(serviceProviderId);
        return ResponseEntity.ok(test);
    }

    @PostMapping("/{testId}/upload-resized-image")
    public ResponseEntity<ServiceProviderTest> uploadResizedImage(@PathVariable Long testId, @RequestParam String resizedImageUrl) {
        ServiceProviderTest test = testService.uploadResizedImage(testId, resizedImageUrl);
        return ResponseEntity.ok(test);
    }

    @PostMapping("/{testId}/submit-typing-test")
    public ResponseEntity<ServiceProviderTest> submitTypingTest(@PathVariable Long testId, @RequestParam String typedText) {
        ServiceProviderTest test = testService.submitTypingTest(testId, typedText);
        return ResponseEntity.ok(test);
    }
}
