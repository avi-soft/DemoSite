package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.component.Constant;
import com.community.api.entity.Examination;
import com.community.api.services.ExaminationService;
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

import static com.community.api.component.Constant.FIND_ALL_EXAMINATIONS_QUERY;

@RestController
@RequestMapping("/examination")
public class ExaminationController {

    private EntityManager entityManager;
    private ResponseService responseService;
    protected ExceptionHandlingImplement exceptionHandling;
    private ExaminationService examinationService;
    public ExaminationController(EntityManager entityManager,ResponseService responseService,ExceptionHandlingImplement exceptionHandling,ExaminationService examinationService) {
        this.responseService=responseService;
        this.entityManager = entityManager;
        this.exceptionHandling=exceptionHandling;
        this.examinationService=examinationService;
    }


    @GetMapping("/get-all-exams")

    public ResponseEntity<?> getAllExaminations() {
        TypedQuery<Examination> query = entityManager.createQuery(FIND_ALL_EXAMINATIONS_QUERY, Examination.class);
        List<Examination> examinations = query.getResultList();
        return responseService.generateResponse(HttpStatus.OK,"List Retrieved Successfully",examinations);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addExamination(@RequestBody Examination examination) throws Exception {
       Examination addedExamination= examinationService.addExamination(examination);
       return responseService.generateResponse(HttpStatus.CREATED,"Examination added successfully",addedExamination);
    }

    @ExceptionHandler( {RuntimeException.class,Exception.class
    })
    public ResponseEntity<?> handleException(Exception e) {
        HttpStatus status;
        String message;

        if (e instanceof RuntimeException) {
            status = HttpStatus.OK;
            message = "Examination list is empty";
        }
        else if(e instanceof Exception)
        {
            status = HttpStatus.BAD_REQUEST;
            message = "examination name cannot be empty";
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

