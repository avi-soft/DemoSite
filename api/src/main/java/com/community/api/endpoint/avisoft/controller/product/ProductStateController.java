package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.component.Constant;
import com.community.api.entity.CustomProductState;
import com.community.api.services.ProductStateService;
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
public class ProductStateController {

    private final ExceptionHandlingService exceptionHandlingService;
    private final ProductStateService productStateService;

    @Autowired
    public ProductStateController(ExceptionHandlingService exceptionHandlingService, ProductStateService productStateService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.productStateService = productStateService;
    }

    @GetMapping("/get-all-product-state")
    public ResponseEntity<?> getAllProductState() {
        try {
            List<CustomProductState> productStateList = productStateService.getAllProductState();
            if (productStateList.isEmpty()) {
                return ResponseService.generateErrorResponse("NO PRODUCT STATE FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("PRODUCT STATES FOUND", productStateList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-product-state-by-id/{productStateId}")
    public ResponseEntity<?> getProductStateById(@PathVariable Long productStateId) {
        try {
            CustomProductState productState = productStateService.getProductStateById(productStateId);
            if (productState == null) {
                return ResponseService.generateErrorResponse("NO PRODUCT STATE FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("PRODUCT STATE FOUND", productState, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-product-state-by-name/{productStateName}")
    public ResponseEntity<?> getProductStateByName(@PathVariable String productStateName) {
        try {

            CustomProductState productState = productStateService.getProductStateByName(productStateName.toUpperCase());
            if (productState == null) {
                return ResponseService.generateErrorResponse("NO PRODUCT STATE FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("PRODUCT STATE FOUND", productState, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
