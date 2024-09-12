package com.community.api.endpoint;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)

public class GlobalExceptionHandler {

    @ExceptionHandler(value = {  HttpRequestMethodNotSupportedException.class })
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResponseEntity<ErrorResponse> handleNotFoundRequests(Exception ex, WebRequest request) {

        return generateErrorResponse("Invalid request body", HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(value = NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex, WebRequest request) {
        return generateErrorResponse("Invalid request body", HttpStatus.BAD_REQUEST);
    }

        public ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Internal Server Error");
        errorResponse.setStatus_code(status.value());
        errorResponse.setStatus(status);
        return new ResponseEntity<>(errorResponse, headers, status);
    }


    @ExceptionHandler(value = { MethodArgumentNotValidException.class })
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        List<String> errorMessages = new ArrayList<>();
        bindingResult.getFieldErrors().forEach(fieldError -> {
            errorMessages.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        });
        return generateErrorResponse("Invalid request parameters: " + errorMessages, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = { BindException.class })
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, WebRequest request) {
        return generateErrorResponse("Invalid request body", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = { NullPointerException.class })
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException ex, WebRequest request) {
        return generateErrorResponse("Null pointer exception", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = { IllegalArgumentException.class })
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return generateErrorResponse("Invalid argument", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = { RuntimeException.class })
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        return generateErrorResponse("Runtime exception " , HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ResponseEntity<ErrorResponse> generateErrorResponse(String message, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(message);
        errorResponse.setStatus_code(status.value());
        errorResponse.setStatus(status);
        return new ResponseEntity<>(errorResponse, status);
    }
}

@Getter
@Setter
class ErrorResponse {
    private String message;
    private int status_code;
    private HttpStatus status;

}