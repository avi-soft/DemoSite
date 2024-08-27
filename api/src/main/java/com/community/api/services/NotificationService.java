package com.community.api.services;

import com.community.api.entity.CustomCustomer;
import com.community.api.services.exception.CustomerDoesNotExistsException;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import java.io.IOException;

@Service
public class NotificationService {

    private EmailService emailService;
    private EntityManager entityManager;
    NotificationService(EmailService emailService,EntityManager entityManager)
    {
        this.emailService= emailService;
        this.entityManager=entityManager;
    }

    public CustomCustomer notifyCustomer(Long customCustomerId) throws CustomerDoesNotExistsException, IOException {
        CustomCustomer customCustomer;
            customCustomer = entityManager.find(CustomCustomer.class, customCustomerId);
            if (customCustomer == null) {
                throw new CustomerDoesNotExistsException("Customer does not exist " + "with id " + customCustomerId);
            }
            emailService.sendExpirationEmail(customCustomer.getEmailAddress(), customCustomer.getFirstName(), customCustomer.getLastName());
        return customCustomer;
    }
}