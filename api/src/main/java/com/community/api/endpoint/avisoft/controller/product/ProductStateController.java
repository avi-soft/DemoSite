package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.component.Constant;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomProductState;
import com.community.api.services.ProductStateService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductStateController {
    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Autowired
    ProductStateService productStateService;

    @GetMapping("/getAllProductState")
    public ResponseEntity<?> getAllProductState() {
        try {
            List<CustomProductState> productStateList = productStateService.getAllProductState();
            return new ResponseEntity<>(productStateList, HttpStatus.OK);
        }catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage() , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getProductStateById/{productStateId}")
    public ResponseEntity<?> getProductStateById(@PathVariable Long productStateId) {
        try {
            CustomProductState productState = productStateService.getProductStateById(productStateId);
            return new ResponseEntity<>(productState, HttpStatus.OK);
        }catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage() , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getProductStateByName/{productStateName}")
    public ResponseEntity<?> getProductStateByName(@PathVariable String productStateName) {
        try {
            CustomProductState productState = productStateService.getProductStateByName(productStateName);
            return new ResponseEntity<>(productState, HttpStatus.OK);
        }catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage() , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
