package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.component.Constant;
import com.community.api.entity.CustomProductRejectionStatus;
import com.community.api.entity.CustomTicketState;
import com.community.api.services.ProductRejectionStatusService;
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
public class ProductRejectionStatusController {

    private final ExceptionHandlingService exceptionHandlingService;
    private final ProductRejectionStatusService productRejectionStatusService;

    @Autowired
    public ProductRejectionStatusController(ExceptionHandlingService exceptionHandlingService, ProductRejectionStatusService productRejectionStatusService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.productRejectionStatusService = productRejectionStatusService;
    }

    @GetMapping("/get-all-product-rejection-status")
    public ResponseEntity<?> getAllProductRejectionStatus() {
        try {
            List<CustomProductRejectionStatus> customTicketStateList = productRejectionStatusService.getAllRejectionStatus();
            if (customTicketStateList.isEmpty()) {
                return ResponseService.generateErrorResponse("NO REJECTION STATUS IS FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("REJECTION STATUS IS FOUND", customTicketStateList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-product-rejection-status-by-rejection-status-id/{rejectionStatusId}")
    public ResponseEntity<?> getProductRejectionStateByRejectionStatusId(@PathVariable Long rejectionStatusId) {
        try {
            CustomProductRejectionStatus customProductRejectionStatus = productRejectionStatusService.getAllRejectionStatusByRejectionStatusId(rejectionStatusId);
            if (customProductRejectionStatus == null) {
                return ResponseService.generateErrorResponse("NO REJECTION STATUS IS FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("REJECTION STATUS IF FOUND", customProductRejectionStatus, HttpStatus.OK);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
