package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.component.Constant;
import com.community.api.entity.CustomJobGroup;
import com.community.api.services.JobGroupService;
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
public class JobGroupController {

    private final ExceptionHandlingService exceptionHandlingService;
    private final JobGroupService jobGroupService;

    @Autowired
    public JobGroupController(ExceptionHandlingService exceptionHandlingService, JobGroupService jobGroupService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.jobGroupService = jobGroupService;
    }

    @GetMapping("/get-all-job-group")
    public ResponseEntity<?> getAllJobGroup() {
        try {
            List<CustomJobGroup> applicationScopeList = jobGroupService.getAllJobGroup();
            if (applicationScopeList.isEmpty()) {
                return ResponseService.generateErrorResponse("No Job Group Found", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("Job Groups Found", applicationScopeList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-job-group-by-id/{jobGroupId}")
    public ResponseEntity<?> getJobGroupById(@PathVariable Long jobGroupId) {
        try {
            CustomJobGroup jobGroup = jobGroupService.getJobGroupById(jobGroupId);
            if (jobGroup == null) {
                return ResponseService.generateErrorResponse("No Job Group Found", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("Job Group Found", jobGroup, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
