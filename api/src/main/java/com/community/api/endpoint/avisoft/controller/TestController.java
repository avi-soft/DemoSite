package com.community.api.endpoint.avisoft.controller;

import com.community.api.component.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private JwtUtil jwtUtil;

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

    @GetMapping("/generate-token")
    public String generateToken(@RequestParam String phoneNumber) {
        return jwtUtil.generateToken(phoneNumber);
    }


}
