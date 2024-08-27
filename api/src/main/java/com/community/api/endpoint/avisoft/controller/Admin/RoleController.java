package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.entity.Role;
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
    @PostMapping("addRole")
    public ResponseEntity<?> addRole(@RequestBody Role role)
    {
        try{
            return roleService.addRole(role);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error aadding role", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/getRoleList")
    public ResponseEntity<?> getRoles() {
        try{
            return new ResponseEntity<>(roleService.findAllRoleList(),HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some updating: " + e.getMessage());
        }
    }
}
