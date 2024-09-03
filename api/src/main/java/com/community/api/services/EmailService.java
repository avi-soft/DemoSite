package com.community.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Service
public class EmailService {

    @Autowired
    @Qualifier("blMailSender")
    private JavaMailSender mailSender;

    @Value("${email.from}")
    private String fromEmail;

    public void sendExpirationEmail(String to, String customerFirstName, String customerLastName) throws IOException {
        String template = loadTemplate("email-templates/expiration-email.txt");
        String messageBody = template
                .replace("{firstName}", customerFirstName)
                .replace("{lastName}", customerLastName);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Your Application Form is About to Expire");
        message.setText(messageBody);
        mailSender.send(message);
    }

    private String loadTemplate(String templateName) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templateName)) {
            if (inputStream == null) {
                throw new IOException("Template file not found: " + templateName);
            }
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name());
            return scanner.useDelimiter("\\A").next();
        }
    }
}
