package com.community.api.endpoint.avisoft.controller;

import com.community.api.component.JwtUtil;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.CustomEmailService;
import com.community.api.services.RateLimiterService;
import io.github.bucket4j.Bucket;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.community.api.services.exception.ExceptionHandlingImplement;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Base64;

@RestController
@RequestMapping("/test")
public class TestController {

    private final RateLimiterService rateLimiterService;

    public TestController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CustomCustomerService customCustomerService;

    @Autowired
    private CustomEmailService emailService;

    @GetMapping("/catch-error")
    public ResponseEntity<String> catcherror() {
        
        try {
            int x = 5 / 0;
        } catch (Exception e) {

            String errorMessage = exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);

        }
        return ResponseEntity.ok("Success");
    }


    @GetMapping("/generate-key")
    public String generateKey() {
        Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Generated Key: " + base64Key);
        return base64Key;
    }

    @GetMapping("/api/rate-limit")
    public String rateLimit(@RequestParam String userId, HttpServletRequest request) {
        Bucket bucket = rateLimiterService.resolveBucket(userId,"/api/rate-limit");

        if (bucket.tryConsume(1)) {
            return "Request successful!";
        } else {
            return "Rate limit exceeded!";
        }
    }

    @PostMapping("/send-mail")
    public ResponseEntity<String> sendEmail(@RequestParam String to,
                                            @RequestParam String subject,
                                            @RequestParam String text) {
        try {
            String result = emailService.sendmail(to, subject, text);
            return ResponseEntity.ok(result);
        } catch (Exception e) {

            System.err.println("Error handling email request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email: " + e.getMessage());
        }
    }

}
