package com.community.api.endpoint.avisoft.controller.upload;


import com.community.api.services.ApiConstants;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@RestController
@RequestMapping("/upload")
public class UploadDocument {

    @Autowired
    private ExceptionHandlingImplement exceptionHandlingService;

    @Autowired
    private ResponseService responseService;

    @RequestMapping("/documents")
    public ResponseEntity<?> uploadDocument(HttpServletRequest request, @RequestBody Map<String, Object> uploadrequest) {

        try{
            if(uploadrequest==null){
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
            String otpEntered = (String) uploadrequest.get("otpEntered");
            Integer role = (Integer) uploadrequest.get("role");
            String countryCode = (String) uploadrequest.get("countryCode");


            if (role == null) {
                return responseService.generateErrorResponse(ApiConstants.ROLE_EMPTY, HttpStatus.BAD_REQUEST);
            }


        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>(ApiConstants.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }
}
