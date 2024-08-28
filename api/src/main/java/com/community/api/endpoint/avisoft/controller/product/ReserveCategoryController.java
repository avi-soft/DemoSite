package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.component.Constant;
import com.community.api.entity.CustomNotifyingAuthority;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.services.NotifyingAuthorityService;
import com.community.api.services.ReserveCategoryService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReserveCategoryController {

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Autowired
    ReserveCategoryService reserveCategoryService;

    @GetMapping("/getAllReserveCategory")
    public ResponseEntity<?> getAllNotifyingAuthority() {
        try {
            List<CustomReserveCategory> authorities = reserveCategoryService.getAllReserveCategory();
            return new ResponseEntity<>(authorities, HttpStatus.OK);
        }catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage() , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
