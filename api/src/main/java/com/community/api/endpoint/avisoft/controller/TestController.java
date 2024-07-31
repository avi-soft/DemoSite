package com.community.api.endpoint.avisoft.controller;

import com.community.api.component.JwtUtil;
import com.community.api.endpoint.avisoft.controller.otpmodule.OtpEndpoint;
import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.services.CustomCustomerService;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.community.api.services.exception.ExceptionHandlingImplement;

import javax.servlet.http.HttpSession;
import java.security.Key;
import java.util.Base64;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CustomCustomerService customCustomerService;

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
    public ResponseEntity<OtpEndpoint.AuthResponse> generateToken(@RequestBody CustomCustomer customerDetails, HttpSession session) {
        String tokenKey = "authToken_" + customerDetails.getMobileNumber();
        String Role = customerDetails.getMobileNumber();
        String existingToken = (String) session.getAttribute(tokenKey);
        System.out.println(existingToken + " existingToken");
        if (existingToken!= null && jwtUtil.validateToken(existingToken, customCustomerService)) {
            return ResponseEntity.ok(new OtpEndpoint.AuthResponse(existingToken));
        } else {
            String newToken = jwtUtil.generateToken(customerDetails.getMobileNumber(),"USER");
            session.setAttribute(tokenKey, newToken);
            return ResponseEntity.ok(new OtpEndpoint.AuthResponse(newToken));
        }

    }
    @GetMapping("/generate-key")
    public String generateKey() {
        Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Generated Key: " + base64Key);
        return base64Key;
    }

}
