package com.community.api.services.exception;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<String> handleError(HttpServletRequest request) {
        // Get the HTTP status code from the request
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            // Handle 404 (Not Found) errors
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Custom 404 message - Resource not found");
        }else {
            // Default case: Handle other errors
            return ResponseEntity.status(statusCode).body("Custom error - Something went wrong(From CustomErrorController.");
        }
    }

}
