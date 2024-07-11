package com.community.api.services;

import com.twilio.exception.ApiException;
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

    @Override
    public void handleApiException(ApiException e) {
        int errorCode = e.getCode();
        String errorMessage = e.getMessage();

        if (errorCode == 21408) {

            throw new RuntimeException("Permission to send SMS not enabled for the region", e);
        } else {

            throw new RuntimeException("Api  Error: " + errorCode + ", Response Body: " + errorMessage, e);
        }

    }

@Override
    public void handleException(Exception e) {
        if (e instanceof ApiException) {
            handleApiException((ApiException) e);
        } else if (e instanceof HttpClientErrorException) {
            handleHttpClientErrorException((HttpClientErrorException) e);
        } else {
            throw new RuntimeException("Unknown Error: " + e.getMessage(), e);
        }
    }
}
