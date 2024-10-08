package com.community.api.endpoint.avisoft.controller.product;

import com.community.api.component.Constant;
import com.community.api.entity.CustomReserveCategory;
import com.community.api.services.ReserveCategoryService;
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
public class ReserveCategoryController {
    private final ExceptionHandlingService exceptionHandlingService;
    private final ReserveCategoryService reserveCategoryService;

    @Autowired
    public ReserveCategoryController(ExceptionHandlingService exceptionHandlingService, ReserveCategoryService reserveCategoryService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.reserveCategoryService = reserveCategoryService;
    }

    @GetMapping("/get-all-reserve-category")
    public ResponseEntity<?> getAllReserveCategory() {
        try {
            List<CustomReserveCategory> authorities = reserveCategoryService.getAllReserveCategory();
            if (authorities.isEmpty()) {
                return ResponseService.generateErrorResponse("No Reserve Category Found", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("Reserve Categories Found", authorities, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("get-reserve-category-by-id/{reserveCategoryId}")
    public ResponseEntity<?> getReserveCategoryById(@PathVariable Long reserveCategoryId) {
        try {
            CustomReserveCategory customReserveCategory = reserveCategoryService.getReserveCategoryById(reserveCategoryId);
            if (customReserveCategory == null) {
                return ResponseService.generateErrorResponse("No Reserve Category Found", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("Reserve Category Found", customReserveCategory, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
