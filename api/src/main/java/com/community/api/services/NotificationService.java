package com.community.api.services;

import com.community.api.endpoint.customer.CustomCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService
{
    @Autowired
    private EmailService emailService;

    public void notifyCustomer(CustomCustomer customer) {
        emailService.sendExpirationEmail(customer.getEmailAddress(), customer.getFirstName(), customer.getLastName());
    }
}
