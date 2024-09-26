package com.community.api.services.exception;

import com.twilio.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExceptionHandlingService implements ExceptionHandlingImplement {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlingService.class);

    @Override
    public void handleHttpError(ResponseEntity<String> response) {
        HttpStatus statusCode = response.getStatusCode();
        String responseBody = response.getBody();
        throw new RuntimeException("HTTP Error: " + statusCode + ", Response Body: " + responseBody);
    }

    @Override
    public String handleHttpClientErrorException(HttpClientErrorException e) {
        HttpStatus statusCode = e.getStatusCode();
        String responseBody = e.getResponseBodyAsString();
        throw new RuntimeException("HTTP Client Error: " + statusCode + ", Response Body: " + responseBody, e);
    }

    @Override
    public String handleApiException(ApiException e) {
        int errorCode = e.getCode();
        String errorMessage = e.getMessage();

        if (errorCode == 21408) {

            throw new RuntimeException("Permission to send SMS not enabled for the region", e);
        } else {

            throw new RuntimeException("Api  Error: " + errorCode + ", Response Body: " + errorMessage, e);
        }

    }

    @Override
    public String handleException(Exception e) {

        logger.error("Exception occurred: ", e);
        if (e instanceof ApiException) {
            return handleApiException((ApiException) e);
        } else if (e instanceof HttpClientErrorException) {
            return handleHttpClientErrorException((HttpClientErrorException) e);
        } else {
            return "Something went wrong: " + e.getMessage();
        }

    }

    public String handleException(HttpStatus status, Exception e) {

        if(status.equals(HttpStatus.BAD_REQUEST)){
            return status + " " + e.getMessage();
        }else if(status.equals(HttpStatus.INTERNAL_SERVER_ERROR)){
            return status + " " + e.getMessage();
        }else{
            return "Something went wrong: " + e.getMessage();
        }

    }
}
