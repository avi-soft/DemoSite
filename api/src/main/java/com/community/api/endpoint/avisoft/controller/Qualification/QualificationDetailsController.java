package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.dto.UpdateQualificationDto;
import com.community.api.entity.QualificationDetails;
import com.community.api.services.QualificationDetailsService;
import com.community.api.services.ResponseService;
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
    public QualificationDetailsController(QualificationDetailsService qualificationDetailsService, ExceptionHandlingImplement exceptionHandling, ResponseService responseService)
    {
        this.qualificationDetailsService = qualificationDetailsService;
        this.exceptionHandling=exceptionHandling;
        this.responseService = responseService;
    }

    @PostMapping("/add/{customCustomerId}")
    public ResponseEntity<?> addQualificationDetail(@PathVariable Long customCustomerId , @Valid @RequestBody QualificationDetails qualificationDetails) throws EntityAlreadyExistsException, ExaminationDoesNotExistsException, CustomerDoesNotExistsException {

            QualificationDetails newQualificationDetails = qualificationDetailsService.addQualificationDetails(customCustomerId , qualificationDetails);
            return ResponseService.generateSuccessResponse("Qualification Details is added successfully",newQualificationDetails,HttpStatus.CREATED);
    }

    @GetMapping("/get-by-customer-id/{customCustomerId}")
    public ResponseEntity<?> getQualificationDetailsById(@PathVariable Long customCustomerId) throws CustomerDoesNotExistsException {
            List<QualificationDetails> qualificationDetails = qualificationDetailsService.getQualificationDetailsByCustomerId(customCustomerId);
            return ResponseService.generateSuccessResponse("Qualification Details are found ",qualificationDetails,HttpStatus.OK);
    }

    @DeleteMapping("/delete/{customCustomerId}/{qualificationDetailId}")
    public ResponseEntity<?> deleteQualificationDetailById(@PathVariable Long customCustomerId, @PathVariable Long qualificationDetailId) throws EntityDoesNotExistsException, CustomerDoesNotExistsException {
            QualificationDetails qualificationDetailsToDelete = qualificationDetailsService.deleteQualificationDetail(customCustomerId,qualificationDetailId);
            return responseService.generateResponse(HttpStatus.OK,"Qualification Detail is deleted successfully", qualificationDetailsToDelete);
    }

    @PutMapping("/update/{customCustomerId}/{qualificationDetailId}")
    public ResponseEntity<?> updateQualificationDetailById(@PathVariable Long customCustomerId, @PathVariable Long qualificationDetailId, @Valid @RequestBody UpdateQualificationDto qualification) throws EntityDoesNotExistsException, EntityAlreadyExistsException, ExaminationDoesNotExistsException, CustomerDoesNotExistsException {
            QualificationDetails qualificationDetailsToUpdate = qualificationDetailsService.updateQualificationDetail( customCustomerId,qualificationDetailId,qualification);
            return responseService.generateResponse(HttpStatus.OK,"Qualification Detail is updated successfully", qualificationDetailsToUpdate);
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
            message = "Qualification Details does not exist";
        }
        else if (e instanceof ExaminationDoesNotExistsException) {
            status = HttpStatus.NOT_FOUND;
            message = "Qualification does not exist";
        }
        else if (e instanceof EntityAlreadyExistsException) {
            status = HttpStatus.BAD_REQUEST;
            message = "Qualification already exists";
        }
        else if (e instanceof RuntimeException) {
            status = HttpStatus.OK;
            message = "Qualification Details list is empty";
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
