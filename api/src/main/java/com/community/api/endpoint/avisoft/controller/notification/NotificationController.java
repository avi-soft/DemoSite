package com.community.api.endpoint.avisoft.controller.notification;

import com.community.api.services.NotificationService;
import com.community.api.services.exception.CustomerDoesNotExistsException;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.io.IOException;

@RestController
@RequestMapping("notification")
public class NotificationController
{
        @Autowired
        private NotificationService notificationService;
        @Autowired
        private EntityManager entityManager;
        @Autowired
        ExceptionHandlingImplement exceptionHandlingImplement;

        @PostMapping("/notify/{customerId}")
        public ResponseEntity<String> notifyCustomer(@PathVariable Long customerId) throws IOException {
            try
            {
                notificationService.notifyCustomer(customerId);
                notificationService.notifyCustomer(customerId);
                return ResponseEntity.ok("Notification sent successfully");
            }
            catch (CustomerDoesNotExistsException customerDoesNotExistsException)
            {
                exceptionHandlingImplement.handleException(customerDoesNotExistsException);
                return ResponseEntity.status(404).body("Customer does not exist with id " + customerId);
            }
            catch (Exception e) {
                exceptionHandlingImplement.handleException(e);
                throw new RuntimeException("An unexpected error occurred while notifying the customer", e);
            }
        }
}
