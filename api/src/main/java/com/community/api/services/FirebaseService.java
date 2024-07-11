package com.community.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class FirebaseService {

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Value("${firebase.api-key}")
    private String firebaseApiKey;

    @Value("${firebase.send-otp-url}")
    private String firebaseSendOtpUrl;

    @Value("${firebase.verify-otp-url}")
    private String firebaseVerifyOtpUrl;

    private final RestTemplate restTemplate;

    public FirebaseService() {
        this.restTemplate = new RestTemplate();
    }

    public String sendOtpToMobile(String mobileNumber) {
        if (mobileNumber == null || mobileNumber.isEmpty()) {
            throw new IllegalArgumentException("Mobile number cannot be null or empty");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + firebaseApiKey);

            String requestBody = "{\"phoneNumber\":\"" + mobileNumber + "\"}";

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);


            System.out.println("Request URL: " + firebaseSendOtpUrl);
            System.out.println("Request Headers: " + headers);
            System.out.println("Request Body: " + requestBody);


            ResponseEntity<String> response = restTemplate.postForEntity(
                    firebaseSendOtpUrl, request, String.class);


            System.out.println(" response: " + response);
            if (response.getStatusCode().is2xxSuccessful()) {
                return "OTP sent successfully";
            } else {
                exceptionHandling.handleHttpError(response);
                return null;
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("Unauthorized access: Please check your API key", e);
            } else {
                exceptionHandling.handleHttpClientErrorException(e);
                return null;
            }
        }
    }

    public String verifyOtp(String mobileNumber, String otp) {
        if (mobileNumber == null || mobileNumber.isEmpty() || otp == null || otp.isEmpty()) {
            throw new IllegalArgumentException("Mobile number and OTP cannot be null or empty");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + firebaseApiKey);

            String requestBody = "{\"phoneNumber\":\"" + mobileNumber + "\",\"code\":\"" + otp + "\"}";

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    firebaseVerifyOtpUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return "OTP verified successfully";
            } else {
                exceptionHandling.handleHttpError(response);
                return null;
            }
        } catch (HttpClientErrorException e) {
            exceptionHandling.handleHttpClientErrorException(e);
            return null;
        }
    }
}
