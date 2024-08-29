package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.component.Constant;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.services.ApplicationScopeService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ApplicationScopeController {

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Autowired
    ApplicationScopeService applicationScopeService;

    @GetMapping("/getAllApplicationScope")
    public ResponseEntity<?> getAllApplicationScope() {
        try {
            List<CustomApplicationScope> applicationScopeList = applicationScopeService.getAllApplicationScope();
            return new ResponseEntity<>(applicationScopeList, HttpStatus.OK);
        }catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage() , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getApplicationScopeById/{applicationScopeId}")
    public ResponseEntity<?> getApplicationScopeById(@PathVariable Long applicationScopeId) {
        try {
            CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(applicationScopeId);
            return new ResponseEntity<>(applicationScope, HttpStatus.OK);
        }catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage() , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
