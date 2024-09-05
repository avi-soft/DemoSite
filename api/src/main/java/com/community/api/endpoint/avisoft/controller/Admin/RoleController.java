package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.entity.Role;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.ParameterResolutionDelegate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/roles",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class RoleController {
    @Autowired
    private RoleService roleService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private ResponseService responseService;
    @PostMapping("add-role")
    public ResponseEntity<?> addRole(@RequestBody Role role)
    {
        try{
            return roleService.addRole(role);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error aadding role", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/get-roles")
    public ResponseEntity<?> getRoles() {
        try{
            return responseService.generateSuccessResponse("Roles",roleService.findAllRoleList(),HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error fetching: "+e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }
}
