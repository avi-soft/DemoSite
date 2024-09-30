package com.community.api.endpoint.avisoft.controller.Gender;

import com.community.api.component.Constant;
import com.community.api.entity.CustomGender;
import com.community.api.services.GenderService;
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
public class GenderController {

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    @Autowired
    GenderService genderService;

    @GetMapping("/get-all-gender")
    public ResponseEntity<?> getAllGender() {
        try {
            List<CustomGender> customGenderList = genderService.getAllGender();
            if (customGenderList.isEmpty()) {
                return ResponseService.generateErrorResponse("NO GENDER IS FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("GENDER FOUND", customGenderList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-gender-by-gender-id/{genderId}")
    public ResponseEntity<?> getGenderByGenderId(@PathVariable Long genderId) {
        try {
            CustomGender customGender = genderService.getGenderByGenderId(genderId);
            if (customGender == null) {
                return ResponseService.generateErrorResponse("NO GENDER FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("GENDER FOUND", customGender, HttpStatus.OK);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
