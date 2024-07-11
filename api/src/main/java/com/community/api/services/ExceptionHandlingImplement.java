package com.community.api.services;

import com.twilio.exception.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;


public interface ExceptionHandlingImplement {
    void handleHttpError(ResponseEntity<String> response);
     void handleHttpClientErrorException(HttpClientErrorException e);

    void handleApiException(ApiException e);

     void handleException(Exception e);
}