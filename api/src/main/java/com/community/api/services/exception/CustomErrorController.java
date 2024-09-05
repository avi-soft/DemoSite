package com.community.api.services.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.community.api.services.ResponseService;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @Autowired
    private static ResponseService responseService;

    @RequestMapping("/error")
    public ResponseEntity<?> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (statusCode == HttpStatus.NOT_FOUND.value()) {
             return responseService.generateErrorResponse("Custom 404 message - Resource not found", HttpStatus.NOT_FOUND);
        }else {
            return responseService.generateErrorResponse("Custom error - Something went wrong(From CustomErrorController.", HttpStatus.NOT_FOUND);

        }
    }

}
