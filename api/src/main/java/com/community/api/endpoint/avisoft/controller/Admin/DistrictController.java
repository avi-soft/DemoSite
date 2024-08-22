package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.entity.Districts;
import com.community.api.services.DistrictService;
import com.community.api.services.PrivilegeService;
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
@RequestMapping(value = "/districts",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class DistrictController {
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private DistrictService districtService;
    @RequestMapping(value = "getDistricts", method = RequestMethod.GET)
    public ResponseEntity<?> getDistricts(@RequestParam String state_code) {
        try {
            List<Districts> names= districtService.findDistrictsByStateCode(state_code);
            return new ResponseEntity<>(names,HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error retrieving list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
