package com.community.api.services;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CustomEmailService {

    @Autowired
    @Qualifier("blMailSender")
    private JavaMailSender emailSender;

    @Value("${email.from}")
    private String fromEmail;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    public String sendmail(String to, String subject, String text) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            return "Email sent successfully";

        }catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error in sending mail ", e);
        }

    }
}