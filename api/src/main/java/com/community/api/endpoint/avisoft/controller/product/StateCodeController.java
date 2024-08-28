package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.component.Constant;
import com.community.api.entity.StateCode;
import com.community.api.services.StateCodeService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StateCodeController {
    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Autowired
    protected StateCodeService stateCodeService;

    @GetMapping("/getAllStates")
    public ResponseEntity<?> getAllNotifyingAuthority() {
        try {
            List<StateCode> states = stateCodeService.getAllStates();
            return new ResponseEntity<>(states, HttpStatus.OK);
        }catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage() , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
