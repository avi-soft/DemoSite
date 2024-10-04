package com.community.api.endpoint.avisoft.controller.Admin;


import com.community.api.entity.Districts;
import com.community.api.entity.StateCode;
import com.community.api.services.DistrictService;
import com.community.api.services.PrivilegeService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/states",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class StateController {
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private ResponseService responseService;
    @RequestMapping(value = "get-states", method = RequestMethod.GET)
    public ResponseEntity<?> getStates() {
        try {
            List<StateCode> names= districtService.findStateList();
            return responseService.generateSuccessResponse("List Retrieved Successfully",names,HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving list",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

