package com.community.api.services;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FirebaseService {

    @Value("${firebase.api.key}")
    private String firebaseApiKey;

    @Value("${firebase.api.url}")
    private String firebaseApiUrl;

    public String sendOtpToMobile(String mobileNumber) throws JSONException {
        // Create a REST template to make the API call
        RestTemplate restTemplate = new RestTemplate();

        // Set up the API request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + firebaseApiKey);

        // Create the request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("mobileNumber", mobileNumber);

        // Make the API call
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(firebaseApiUrl + "/send-otp", request, String.class);

        // Return the response
        return response.getBody();
    }

    public String verifyOtp(String mobileNumber, String otp) throws JSONException {

        // Create a REST template to make the API call
        RestTemplate restTemplate = new RestTemplate();

        // Set up the API request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + firebaseApiKey);

        // Create the request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("mobileNumber", mobileNumber);
        requestBody.put("otp", otp);

        // Make the API call
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(firebaseApiUrl + "/verify-otp", request, String.class);

        // Return the response
        return response.getBody();
    }
}