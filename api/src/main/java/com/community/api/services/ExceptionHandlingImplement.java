package com.community.api.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;


public interface ExceptionHandlingImplement {
    void handleHttpError(ResponseEntity<String> response);
     void handleHttpClientErrorException(HttpClientErrorException e);
}