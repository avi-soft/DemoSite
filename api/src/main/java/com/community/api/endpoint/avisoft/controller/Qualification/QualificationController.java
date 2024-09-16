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
import javax.transaction.Transactional;
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
        if(qualifications.isEmpty())
        {
            return responseService.generateResponse(HttpStatus.OK,"Qualification List is Empty", qualifications);
        }
        return responseService.generateResponse(HttpStatus.OK,"Qualification List Retrieved Successfully", qualifications);
    }


    @PostMapping("/add")
    public ResponseEntity<?> addQualification(@RequestBody Qualification qualification) throws Exception {
        try
        {
            Qualification addedQualification = qualificationService.addQualification(qualification);
            return responseService.generateResponse(HttpStatus.CREATED,"Qualification added successfully", addedQualification);
        }
        catch (IllegalArgumentException e) {
            return responseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e)
        {
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }
}

