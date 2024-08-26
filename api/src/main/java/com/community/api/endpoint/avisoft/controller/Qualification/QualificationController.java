package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.entity.Qualification;
import com.community.api.services.QualificationService;
import com.community.api.services.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping(value = "/qualification")
public class QualificationController
{
    protected QualificationService qualificationService;
    protected ExceptionHandlingImplement exceptionHandling;

    public QualificationController(QualificationService qualificationService,ExceptionHandlingImplement exceptionHandling)
    {
        this.qualificationService=qualificationService;
        this.exceptionHandling=exceptionHandling;
    }

    @PostMapping("/addQualification/{customCustomerId}")
    public ResponseEntity<?> addQualification( @PathVariable Long customCustomerId ,@RequestBody Qualification qualification)  {
        try
        {
            Qualification newQualification= qualificationService.addQualification(customCustomerId,qualification);
            return  ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success",true, "message", "Qualification is added successfully.", "qualification", newQualification));
        }
        catch (EntityAlreadyExistsException exception) {
            exceptionHandling.handleException(exception);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Qualification already exist with examination name"+" " +qualification.getExaminationName());
        } catch (EntityDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer does not exist with customer Id"+" "+customCustomerId);
        } catch (ExaminationDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Examination does not found with examinationName"+" " + qualification.getExaminationName());
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong");
        }
    }
    @GetMapping("/getAllQualifications")
    public ResponseEntity<List<Qualification>> getAllQualifications() throws Exception {
        if(qualificationService.getAllQualifications()!=null)
        {
            List<Qualification> qualifications=qualificationService.getAllQualifications();
            return ResponseEntity.status(HttpStatus.OK).body(qualifications);
        }
        else {
            throw new Exception("Qualification list is empty");
        }

    }

    @GetMapping("/getQualificationById/{customCustomerId}/{qualificationId}")
    public ResponseEntity<?> getQualificationById(@PathVariable Long customCustomerId,@PathVariable Long qualificationId) throws EntityDoesNotExistsException {
        try
        {
            Qualification qualificationToFind = qualificationService.getQualificationById(customCustomerId,qualificationId);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("success",true, "message", "Qualification is founded.", "qualification", qualificationToFind));
        }
        catch(EntityDoesNotExistsException exception )
        {
            exceptionHandling.handleException(exception);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Qualification does not exists with id "+ qualificationId+" for customer with id "+ customCustomerId);
        } catch (CustomerDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer does not exists with id "+ customCustomerId);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong");
        }
    }

    @DeleteMapping("/deleteQualification/{qualificationId}")
    public ResponseEntity<?> deleteQualificationById(@PathVariable Long customCustomerId,@PathVariable Long qualificationId) throws EntityDoesNotExistsException {
        try
        {
            Qualification qualificationToDelete = qualificationService.deleteQualification(customCustomerId,qualificationId);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("success",true, "message", "Qualification is deleted successfully.", "qualification", qualificationToDelete));
        }
        catch(EntityDoesNotExistsException exception )
        {
            exceptionHandling.handleException(exception);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( "Qualification does not exists with id "+ qualificationId);
        }
        catch (CustomerDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer does not exists with id "+ customCustomerId);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong");
        }
    }

    @PutMapping("/updateQualification/{customCustomerId}/{qualificationId}")
    public ResponseEntity<?> updateQualificationById(@PathVariable Long customCustomerId,@PathVariable Long qualificationId, @RequestBody Qualification qualification) throws EntityDoesNotExistsException {
        try
        {
            Qualification qualificationToUpdate = qualificationService.updateQualification( customCustomerId,qualificationId,qualification);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("success",true, "message", "Qualification is updated successfully.", "qualification", qualificationToUpdate));
        }
        catch(EntityDoesNotExistsException exception )
        {
            exceptionHandling.handleException(exception);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Qualification does not exists with id "+ qualificationId);
        }
        catch (EntityAlreadyExistsException exception) {
            exceptionHandling.handleException(exception);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Qualification already exists");
        } catch (ExaminationDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( "Examination does not exists with Examination name "+ qualification.getExaminationName());
        } catch (CustomerDoesNotExistsException e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer does not exists with customer id "+ qualification.getExaminationName());
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong");
        }
    }
}
