package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.dto.UpdateQualificationDto;
import com.community.api.entity.Qualification;
import com.community.api.services.ApiConstants;
import com.community.api.services.QualificationService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(value = "/qualification")
public class QualificationController
{
    protected QualificationService qualificationService;
    protected ExceptionHandlingImplement exceptionHandling;
    private ResponseService responseService;
    public QualificationController(QualificationService qualificationService, ExceptionHandlingImplement exceptionHandling, ResponseService responseService)
    {
        this.qualificationService=qualificationService;
        this.exceptionHandling=exceptionHandling;
        this.responseService = responseService;
    }

    @PostMapping("/add/{customCustomerId}")
    public ResponseEntity<?> addQualification( @PathVariable Long customCustomerId ,@Valid @RequestBody Qualification qualification) throws EntityAlreadyExistsException, ExaminationDoesNotExistsException, CustomerDoesNotExistsException {

            Qualification newQualification= qualificationService.addQualification(customCustomerId ,qualification);
            return responseService.generateResponse(HttpStatus.CREATED,"Qualification is added successfully",newQualification);
    }

    @GetMapping("/get-qualifications-by-customer-id/{customCustomerId}")
    public ResponseEntity<?> getQualificationById(@PathVariable Long customCustomerId) throws CustomerDoesNotExistsException {
            List<Qualification> qualifications = qualificationService.getQualificationsByCustomerId(customCustomerId);
            return responseService.generateResponse(HttpStatus.OK,"Qualifications are found ",qualifications);
    }

    @DeleteMapping("/delete/{customCustomerId}/{qualificationId}")
    public ResponseEntity<?> deleteQualificationById(@PathVariable Long customCustomerId,@PathVariable Long qualificationId) throws EntityDoesNotExistsException, CustomerDoesNotExistsException {
            Qualification qualificationToDelete = qualificationService.deleteQualification(customCustomerId,qualificationId);
        return responseService.generateResponse(HttpStatus.OK,"Qualification is deleted successfully",qualificationToDelete);
    }

    @PutMapping("/update/{customCustomerId}/{qualificationId}")
    public ResponseEntity<?> updateQualificationById(@PathVariable Long customCustomerId,@PathVariable Long qualificationId, @Valid @RequestBody UpdateQualificationDto qualification) throws EntityDoesNotExistsException, EntityAlreadyExistsException, ExaminationDoesNotExistsException, CustomerDoesNotExistsException {
            Qualification qualificationToUpdate = qualificationService.updateQualification( customCustomerId,qualificationId,qualification);
        return responseService.generateResponse(HttpStatus.OK,"Qualification is updated successfully",qualificationToUpdate);
    }

    @ExceptionHandler({CustomerDoesNotExistsException.class, EntityAlreadyExistsException.class, ExaminationDoesNotExistsException.class, EntityDoesNotExistsException.class, RuntimeException.class})
    public ResponseEntity<?> handleException(Exception e) {
        HttpStatus status;
        String message;

        if (e instanceof CustomerDoesNotExistsException) {
            status = HttpStatus.NOT_FOUND;
            message = "Customer does not exist";
        }
        else if (e instanceof EntityDoesNotExistsException) {
            status = HttpStatus.NOT_FOUND;
            message = "Qualification does not exist";
        }
        else if (e instanceof ExaminationDoesNotExistsException) {
            status = HttpStatus.NOT_FOUND;
            message = "Examination does not exist";
        }
        else if (e instanceof EntityAlreadyExistsException) {
            status = HttpStatus.BAD_REQUEST;
            message = "Examination already exists";
        }
        else if (e instanceof RuntimeException) {
            status = HttpStatus.OK;
            message = "Qualification list is empty";
        } else {
            status = HttpStatus.BAD_REQUEST;
            message = "Some error occurred";
        }

        exceptionHandling.handleException(e);
        return responseService.generateErrorResponse(message + ": " + e.getMessage(), status);
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
