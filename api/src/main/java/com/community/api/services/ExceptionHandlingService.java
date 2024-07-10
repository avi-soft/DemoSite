package com.community.api.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class ExceptionHandlingService implements ExceptionHandlingImplement{
    @Override
    public  void handleHttpError(ResponseEntity<String> response) {
        HttpStatus statusCode = response.getStatusCode();
        String responseBody = response.getBody();
        throw new RuntimeException("HTTP Error: " + statusCode + ", Response Body: " + responseBody);
    }

    @Override
        public void handleHttpClientErrorException(HttpClientErrorException e) {
        HttpStatus statusCode = e.getStatusCode();
        String responseBody = e.getResponseBodyAsString();
        throw new RuntimeException("HTTP Client Error: " + statusCode + ", Response Body: " + responseBody, e);
    }
}
