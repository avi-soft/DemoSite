package com.community.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    @Qualifier("blMailSender")
    private JavaMailSender mailSender;

    public void sendExpirationEmail(String to, String customerFirstName,String customerLastName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("simranjit.kaur@avisoft.io");
        message.setTo(to);
        message.setSubject("Your Application Form is About to Expire");
        message.setText("Dear " + customerFirstName +" "+customerLastName+ ",\n\n"
                + "This is a reminder that your application form is about to expire"
                + "Please take necessary action to avoid any inconvenience.\n\n"
                + "Best regards");

        mailSender.send(message);
    }
}
