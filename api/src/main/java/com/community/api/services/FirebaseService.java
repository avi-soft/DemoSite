package com.community.api.services;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class FirebaseService {

    @Value("${firebase.api.key}")
    private String firebaseApiKey;

    @Value("${firebase.api.url}")
    private String firebaseApiUrl;

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

            JSONObject requestBody = new JSONObject();
            requestBody.put("mobileNumber", mobileNumber);

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(firebaseApiUrl + "/send-otp", request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                handleHttpError(response);
                return null; // or throw an exception based on your error handling strategy
            }
        } catch (JSONException e) {
            throw new RuntimeException("Failed to create JSON request", e);
        } catch (HttpClientErrorException e) {
            handleHttpClientErrorException(e);
            return null; // or throw an exception based on your error handling strategy
        }
    }

    public String verifyOtp(String mobileNumber, String otp) {
        if (mobileNumber == null || mobileNumber.isEmpty()) {
            throw new IllegalArgumentException("Mobile number cannot be null or empty");
        }
        if (otp == null || otp.isEmpty()) {
            throw new IllegalArgumentException("OTP cannot be null or empty");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + firebaseApiKey);

            JSONObject requestBody = new JSONObject();
            requestBody.put("mobileNumber", mobileNumber);
            requestBody.put("otp", otp);

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(firebaseApiUrl + "/verify-otp", request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                handleHttpError(response);
                return null; // or throw an exception based on your error handling strategy
            }
        } catch (JSONException e) {
            throw new RuntimeException("Failed to create JSON request", e);
        } catch (HttpClientErrorException e) {
            handleHttpClientErrorException(e);
            return null; // or throw an exception based on your error handling strategy
        }
    }

    private void handleHttpError(ResponseEntity<String> response) {
        HttpStatus statusCode = response.getStatusCode();
        String responseBody = response.getBody(); // You can log or process the response body if needed
        throw new RuntimeException("HTTP Error: " + statusCode + ", Response Body: " + responseBody);
    }

    private void handleHttpClientErrorException(HttpClientErrorException e) {
        HttpStatus statusCode = e.getStatusCode();
        String responseBody = e.getResponseBodyAsString(); // You can log or process the response body if needed
        throw new RuntimeException("HTTP Client Error: " + statusCode + ", Response Body: " + responseBody, e);
    }
}
