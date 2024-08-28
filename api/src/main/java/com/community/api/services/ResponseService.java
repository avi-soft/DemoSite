package com.community.api.services;

import com.community.api.entity.ErrorResponse;
import com.community.api.entity.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ResponseService {
    public <T> ResponseEntity<SuccessResponse<T>> generateSuccessResponse(String message, T data, HttpStatus status) {
        SuccessResponse<T> successResponse = new SuccessResponse<>();
        successResponse.setStatus(status);
        successResponse.setData(data);
        successResponse.setStatus_code(status.value());
        successResponse.setMessage(message);
        return new ResponseEntity<>(successResponse, status);
    }

    public ResponseEntity<ErrorResponse> generateErrorResponse(String message,HttpStatus status)
    {
        ErrorResponse errorResponse=new ErrorResponse();
        errorResponse.setMessage(message);
        errorResponse.setStatus_code(status.value());
        errorResponse.setStatus(status);
        return new ResponseEntity<>(errorResponse,status);
    }
}
