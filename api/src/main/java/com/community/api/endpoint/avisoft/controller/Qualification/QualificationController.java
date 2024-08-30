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
    @Autowired
    private ResponseService responseService;
    public QualificationController(QualificationService qualificationService,ExceptionHandlingImplement exceptionHandling)
    {
        this.qualificationService=qualificationService;
        this.exceptionHandling=exceptionHandling;
    }

    @PostMapping("/add/{customCustomerId}")
    public ResponseEntity<?> addQualification( @PathVariable Long customCustomerId ,@Valid @RequestBody Qualification qualification) {
        try
        {
            Qualification newQualification= qualificationService.addQualification(customCustomerId ,qualification);
            return responseService.generateSuccessResponse("Qualification is added successfully",newQualification ,HttpStatus.OK);

        }
        catch (CustomerDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer does not exist with customer Id"+" "+customCustomerId);
        }
        catch (EntityAlreadyExistsException exception) {
            exceptionHandling.handleException(exception);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Qualification already exist with examination name"+" " +qualification.getExaminationName());
        }
       catch (ExaminationDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Examination does not found with examinationName"+" " + qualification.getExaminationName(), HttpStatus.NOT_FOUND);

        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error occurred"+ e.getMessage(), HttpStatus.BAD_REQUEST);

        }
    }

    @GetMapping("/get-qualifications-by-customer-id/{customCustomerId}")
    public ResponseEntity<?> getQualificationById(@PathVariable Long customCustomerId) {
        try
        {
            List<Qualification> qualifications = qualificationService.getQualificationsByCustomerId(customCustomerId);
            return responseService.generateSuccessResponse("Qualifications are found .",qualifications ,HttpStatus.OK);

        }
        catch (RuntimeException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Customer with id "+customCustomerId+" does not have any qualification", HttpStatus.NOT_FOUND);

        }
        catch (CustomerDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Customer does not exists with id "+ customCustomerId, HttpStatus.NOT_FOUND);

        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error occurred"+ e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{customCustomerId}/{qualificationId}")
    public ResponseEntity<?> deleteQualificationById(@PathVariable Long customCustomerId,@PathVariable Long qualificationId) throws EntityDoesNotExistsException {
        try
        {
            Qualification qualificationToDelete = qualificationService.deleteQualification(customCustomerId,qualificationId);
            return responseService.generateSuccessResponse("Qualification is deleted successfully.",qualificationToDelete ,HttpStatus.OK);

        }
        catch(EntityDoesNotExistsException exception )
        {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Qualification does not exists with id "+ qualificationId, HttpStatus.NOT_FOUND);
        }
        catch (CustomerDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Customer does not exists with id "+ customCustomerId, HttpStatus.NOT_FOUND);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error occurred"+ e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/{customCustomerId}/{qualificationId}")
    public ResponseEntity<?> updateQualificationById(@PathVariable Long customCustomerId,@PathVariable Long qualificationId, @Valid @RequestBody UpdateQualificationDto qualification) throws EntityDoesNotExistsException {
        try
        {
            Qualification qualificationToUpdate = qualificationService.updateQualification( customCustomerId,qualificationId,qualification);
            return responseService.generateSuccessResponse("Qualification is updated successfully.",qualificationToUpdate ,HttpStatus.OK);
        }
        catch(EntityDoesNotExistsException exception )
        {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Qualification does not exists with id "+ qualificationId, HttpStatus.NOT_FOUND);

        }
        catch (EntityAlreadyExistsException exception) {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Qualification already exists", HttpStatus.NOT_FOUND);

        } catch (ExaminationDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Examination does not exists with Examination name "+ qualification.getExaminationName(), HttpStatus.NOT_FOUND);

        } catch (CustomerDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Customer does not exists with customer id "+ customCustomerId, HttpStatus.NOT_FOUND);

        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error occurred"+ e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (existingValue, newValue) -> existingValue + ", " + newValue // Merge messages if there are multiple errors for the same field
                ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("validationErrors", errors));
    }

}
