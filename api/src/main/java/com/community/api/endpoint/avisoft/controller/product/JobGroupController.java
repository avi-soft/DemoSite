package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.component.Constant;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomJobGroup;
import com.community.api.services.ApplicationScopeService;
import com.community.api.services.JobGroupService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JobGroupController {

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Autowired
    JobGroupService jobGroupService;

    @GetMapping("/getAllJobGroup")
    public ResponseEntity<?> getAllJobGroup() {
        try {
            List<CustomJobGroup> applicationScopeList = jobGroupService.getAllJobGroup();
            return new ResponseEntity<>(applicationScopeList, HttpStatus.OK);
        }catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage() , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
