package com.community.api.services.exception;

import com.twilio.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;


public interface ExceptionHandlingImplement {
    void handleHttpError(ResponseEntity<String> response);

    String handleHttpClientErrorException(HttpClientErrorException e);

    String handleApiException(ApiException e);

    String handleException(Exception e);

    String handleException(HttpStatus status, Exception e);
}