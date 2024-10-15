package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.entity.Privileges;
import com.community.api.services.PrivilegeService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import javax.transaction.Transactional;

@RestController
@RequestMapping(value = "/privileges",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class PrivilegesController {
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private PrivilegeService privilegeService;
    @Autowired
    private ResponseService responseService;
    @Transactional
    @RequestMapping(value = "assign-privilege", method = RequestMethod.POST)
    public ResponseEntity<?> assignPrivilege(@RequestParam int privilege_id, @RequestParam Long id, @RequestParam int role_id) {
        try {
           return privilegeService.assignPrivilege(privilege_id,id,role_id);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error assigning privilege", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "remove-privilege", method = RequestMethod.PATCH)
    public ResponseEntity<?> removePrivilege(@RequestParam int privilege_id, @RequestParam Long id, @RequestParam int role_id) {
        try {
            return privilegeService.removePrivilege(privilege_id, id, role_id);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error removing ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @RequestMapping(value = "create-privilege", method = RequestMethod.POST)
    public ResponseEntity<?> createPrivilege(@RequestBody Privileges privilege) {
        try {

            return privilegeService.createPrivilege(privilege);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error removing ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping(value = "get-privileges-for-service-provider", method = RequestMethod.GET)
    public ResponseEntity<?> getAllPrivileges(@RequestParam Long serviceProviderId) {
        try {
            return responseService.generateSuccessResponse("Data",privilegeService.getServiceProviderPrivilege(serviceProviderId),HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error removing ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

