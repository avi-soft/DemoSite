package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.component.Constant;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.services.ApplicationScopeService;
import com.community.api.services.ResponseService;
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

    private final ExceptionHandlingService exceptionHandlingService;
    private final ApplicationScopeService applicationScopeService;

    @Autowired
    public ApplicationScopeController(ExceptionHandlingService exceptionHandlingService, ApplicationScopeService applicationScopeService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.applicationScopeService = applicationScopeService;
    }

    @GetMapping("/get-all-application-scope")
    public ResponseEntity<?> getAllApplicationScope() {
        try {
            List<CustomApplicationScope> applicationScopeList = applicationScopeService.getAllApplicationScope();
            if (applicationScopeList.isEmpty()) {
                return ResponseService.generateErrorResponse("No Application Scope Found", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("Application Scopes Found", applicationScopeList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-application-scope-by-id/{applicationScopeId}")
    public ResponseEntity<?> getApplicationScopeById(@PathVariable Long applicationScopeId) {
        try {
            CustomApplicationScope applicationScope = applicationScopeService.getApplicationScopeById(applicationScopeId);
            if (applicationScope == null) {
                return ResponseService.generateErrorResponse("No Application Scope Found", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("Application Scope Found", applicationScope, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
