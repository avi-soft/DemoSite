package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.entity.Qualification;
import com.community.api.services.QualificationService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.community.api.component.Constant.FIND_ALL_QUALIFICATIONS_QUERY;

@RestController
@RequestMapping("/qualification")
public class QualificationController {

    private EntityManager entityManager;
    private ResponseService responseService;
    protected ExceptionHandlingImplement exceptionHandling;
    private QualificationService qualificationService;
    public QualificationController(EntityManager entityManager, ResponseService responseService, ExceptionHandlingImplement exceptionHandling, QualificationService qualificationService) {
        this.responseService=responseService;
        this.entityManager = entityManager;
        this.exceptionHandling=exceptionHandling;
        this.qualificationService = qualificationService;
    }


    @GetMapping("/get-all-qualifications")

    public ResponseEntity<?> getAllQualifications() {
        TypedQuery<Qualification> query = entityManager.createQuery(FIND_ALL_QUALIFICATIONS_QUERY, Qualification.class);
        List<Qualification> qualifications = query.getResultList();
        return responseService.generateResponse(HttpStatus.OK,"Qualification List Retrieved Successfully", qualifications);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addQualification(@RequestBody Qualification qualification) throws Exception {
       Qualification addedQualification = qualificationService.addQualification(qualification);
       return responseService.generateResponse(HttpStatus.CREATED,"Qualification added successfully", addedQualification);
    }

    @ExceptionHandler( {RuntimeException.class,Exception.class
    })
    public ResponseEntity<?> handleException(Exception e) {
        HttpStatus status;
        String message;

        if (e instanceof RuntimeException) {
            status = HttpStatus.OK;
            message = "Qualification list is empty";
        }
        else if(e instanceof Exception)
        {
            status = HttpStatus.BAD_REQUEST;
            message = "qualification name cannot be empty";
        }
        else{
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

