package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.entity.Districts;
import com.community.api.entity.ErrorResponse;
import com.community.api.entity.SuccessResponse;
import com.community.api.services.DistrictService;
import com.community.api.services.PrivilegeService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping(value = "/districts",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class DistrictController {
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private ResponseService responseService;
    @RequestMapping(value = "get-districts", method = RequestMethod.GET)
    public ResponseEntity<?> getDistricts(@RequestParam String state_code) {
        try {
            if(state_code==null)
                return responseService.generateErrorResponse("Empty value for State Code passed",HttpStatus.BAD_REQUEST);
            List<Districts> names= districtService.findDistrictsByStateCode(state_code);
            if(names.isEmpty()) {
                return responseService.generateErrorResponse("No data found",HttpStatus.NOT_FOUND);
            }
            return responseService.generateSuccessResponse("List retrieved successfully",names,HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
