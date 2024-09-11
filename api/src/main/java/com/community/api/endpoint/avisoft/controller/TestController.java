package com.community.api.endpoint.avisoft.controller;
import com.community.api.component.JwtUtil;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.DocumentStorageService;
import com.community.api.services.RateLimiterService;
import io.github.bucket4j.Bucket;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.community.api.services.exception.ExceptionHandlingImplement;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Base64;

@RestController
@RequestMapping("/test")
public class TestController {

    private final RateLimiterService rateLimiterService;

    private ExceptionHandlingImplement exceptionHandling;

    private JwtUtil jwtUtil;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DocumentStorageService documentStorageService;


    private CustomCustomerService customCustomerService;

    public TestController(RateLimiterService rateLimiterService, ExceptionHandlingImplement exceptionHandling, JwtUtil jwtUtil, CustomCustomerService customCustomerService) {
        this.rateLimiterService = rateLimiterService;
        this.exceptionHandling = exceptionHandling;
        this.jwtUtil = jwtUtil;
        this.customCustomerService = customCustomerService;
    }


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
        Bucket bucket = rateLimiterService.resolveBucket(userId, "/api/rate-limit");

        if (bucket.tryConsume(1)) {
            return "Request successful!";
        } else {
            return "Rate limit exceeded!";
        }
    }

    @PostMapping("/api/data-entry")
    public String dataEntry(@RequestParam String userId) {
        documentStorageService.saveAllDocumentTypes();
        return "Documents inserted successfully";
    }


    @PostMapping("/alter/document")
    @Transactional
    public String alterDocument(@RequestParam String userId) {
        String sql = "ALTER TABLE Document ADD COLUMN role INTEGER";

        try {
            entityManager.createNativeQuery(sql).executeUpdate();
            return "Column 'role' added to Document table successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while altering the Document table";

        }
    }
}
