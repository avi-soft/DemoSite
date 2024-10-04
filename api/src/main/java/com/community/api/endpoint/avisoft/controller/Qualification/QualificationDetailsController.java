package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.UpdateQualificationDto;
import com.community.api.entity.QualificationDetails;
import com.community.api.services.QualificationDetailsService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/qualification-details")
public class QualificationDetailsController
{
    protected QualificationDetailsService qualificationDetailsService;
    protected ExceptionHandlingImplement exceptionHandling;
    private ResponseService responseService;
    private JwtUtil jwtTokenUtil;
    private RoleService roleService;
    public QualificationDetailsController(QualificationDetailsService qualificationDetailsService, ExceptionHandlingImplement exceptionHandling, ResponseService responseService,JwtUtil jwtTokenUtil, RoleService roleService)
    {
        this.qualificationDetailsService = qualificationDetailsService;
        this.exceptionHandling=exceptionHandling;
        this.responseService = responseService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.roleService = roleService;
    }

    @PostMapping("/add/{customCustomerId}")
    public ResponseEntity<?> addQualificationDetail(@PathVariable Long customCustomerId , @Valid @RequestBody QualificationDetails qualificationDetails, @RequestHeader(value = "Authorization") String authHeader) throws EntityAlreadyExistsException, ExaminationDoesNotExistsException, CustomerDoesNotExistsException {
        String role=null;
        try{
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
             role = roleService.getRoleByRoleId(roleId).getRole_name();
            QualificationDetails newQualificationDetails = qualificationDetailsService.addQualificationDetails(customCustomerId , qualificationDetails,role);
            return ResponseService.generateSuccessResponse("Qualification Details is added successfully for "+role,newQualificationDetails,HttpStatus.CREATED);
        }
        catch (CustomerDoesNotExistsException e)
        {
            if(role.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
            {
                return ResponseService.generateErrorResponse("Service Provider does not exist",HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateErrorResponse("Customer does not exist",HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (EntityAlreadyExistsException e)
        {
            return ResponseService.generateErrorResponse("Qualification already exists",HttpStatus.BAD_REQUEST);
        }
        catch (ExaminationDoesNotExistsException e)
        {
            return ResponseService.generateErrorResponse("Qualification does not exist",HttpStatus.NOT_FOUND);
        }
        catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-by-customer-id/{customCustomerId}")
    public ResponseEntity<?> getQualificationDetailsById(@PathVariable Long customCustomerId, @RequestHeader(value = "Authorization") String authHeader) throws CustomerDoesNotExistsException ,EntityDoesNotExistsException{
        String role=null;
        try
        {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
             role = roleService.getRoleByRoleId(roleId).getRole_name();
            List<Map<String, Object>> qualificationDetails = qualificationDetailsService.getQualificationDetailsByCustomerId(customCustomerId,role);
            if(qualificationDetails.isEmpty())
            {
                return ResponseService.generateSuccessResponse("Qualification Details list is empty for "+role,qualificationDetails,HttpStatus.OK);
            }
            return ResponseService.generateSuccessResponse("Qualification Details are found for "+role,qualificationDetails,HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (CustomerDoesNotExistsException e)
        {
            if(role.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
            {
                return ResponseService.generateErrorResponse("Service Provider does not exist",HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateErrorResponse("Customer does not exist",HttpStatus.NOT_FOUND);
        } catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{customCustomerId}/{qualificationDetailId}")
    public ResponseEntity<?> deleteQualificationDetailById(@PathVariable Long customCustomerId, @PathVariable Long qualificationDetailId,@RequestHeader(value = "Authorization") String authHeader) throws EntityDoesNotExistsException, CustomerDoesNotExistsException {
        String role=null;
        try
        {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
             role = roleService.getRoleByRoleId(roleId).getRole_name();
            QualificationDetails qualificationDetailsToDelete = qualificationDetailsService.deleteQualificationDetail(customCustomerId,qualificationDetailId,role);
            return responseService.generateResponse(HttpStatus.OK,"Qualification Detail is deleted successfully for "+ role, qualificationDetailsToDelete);
        }
        catch (CustomerDoesNotExistsException e)
        {
            if(role.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
            {
                return ResponseService.generateErrorResponse("Service Provider does not exist",HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateErrorResponse("Customer does not exist",HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (EntityDoesNotExistsException e)
        {
            return ResponseService.generateErrorResponse("Qualification Details does not exist",HttpStatus.NOT_FOUND);
        }
        catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/{customCustomerId}/{qualificationDetailId}")
    public ResponseEntity<?> updateQualificationDetailById(@PathVariable Long customCustomerId, @PathVariable Long qualificationDetailId, @Valid @RequestBody UpdateQualificationDto qualification,@RequestHeader(value = "Authorization") String authHeader) throws EntityDoesNotExistsException, EntityAlreadyExistsException, ExaminationDoesNotExistsException, CustomerDoesNotExistsException {
        String role=null;
        try
        {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
             role = roleService.getRoleByRoleId(roleId).getRole_name();
            QualificationDetails qualificationDetailsToUpdate = qualificationDetailsService.updateQualificationDetail( customCustomerId,qualificationDetailId,qualification,role);
            return responseService.generateResponse(HttpStatus.OK,"Qualification Detail is updated successfully for "+ role, qualificationDetailsToUpdate);
        }
        catch (CustomerDoesNotExistsException e)
        {
            if(role.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
            {
                return ResponseService.generateErrorResponse("Service Provider does not exist",HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateErrorResponse("Customer does not exist",HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        catch (ExaminationDoesNotExistsException e)
        {
            return ResponseService.generateErrorResponse("Qualification does not exist",HttpStatus.NOT_FOUND);
        }
        catch (EntityDoesNotExistsException e)
        {
            return ResponseService.generateErrorResponse("Qualification Details does not exist",HttpStatus.NOT_FOUND);
        }
        catch (EntityAlreadyExistsException e)
        {
            return ResponseService.generateErrorResponse("Qualification already exists",HttpStatus.BAD_REQUEST);
        }
        catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>>handlesValidationErrors(MethodArgumentNotValidException exception) {
        HttpStatus status;
        List<String> errors = exception.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
        Map<String,Object>responseData=new HashMap<>();
        responseData.put("message",errors);
        status= HttpStatus.BAD_REQUEST;
        responseData.put("status_code",400);
        responseData.put("status",status);
        return ResponseEntity.status(status).body(responseData);
    }

}