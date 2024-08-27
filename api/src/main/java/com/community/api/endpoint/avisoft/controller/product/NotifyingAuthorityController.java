package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.component.Constant;
import com.community.api.entity.CustomNotifyingAuthority;
import com.community.api.services.NotifyingAuthorityService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NotifyingAuthorityController {

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Autowired
    NotifyingAuthorityService notifyingAuthorityService;

    @PostMapping("/getAllNotifyingAuthority")
    public ResponseEntity<?> getAllNotifyingAuthority() {
        try {
            List<CustomNotifyingAuthority> authorities = notifyingAuthorityService.getAllNotifyingAuthority();
            return new ResponseEntity<>(authorities, HttpStatus.OK);
        }catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage() , HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
