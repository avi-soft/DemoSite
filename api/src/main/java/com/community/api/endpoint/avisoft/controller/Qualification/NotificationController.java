package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.entity.CustomCustomer;
import com.community.api.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController
{
        @Autowired
        private NotificationService notificationService;
        @Autowired
        private EntityManager entityManager;

        @PostMapping("/notify/{customerId}")
        public ResponseEntity<String> notifyCustomer(@PathVariable Long customerId) {
            CustomCustomer customer = entityManager.find(CustomCustomer.class,customerId);
            if (customer != null) {
                notificationService.notifyCustomer(customer);
                return ResponseEntity.ok("Notification sent successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        }
}
