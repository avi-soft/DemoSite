package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.endpoint.customer.Qualification;
import com.community.api.services.QualificationService;
import com.community.api.services.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping(value = "/qualification")
public class QualificationController
{
    protected QualificationService qualificationService;
    protected ExceptionHandlingService exceptionHandlingService;
    public QualificationController(QualificationService qualificationService,ExceptionHandlingService exceptionHandlingService)
    {
        this.qualificationService=qualificationService;
        this.exceptionHandlingService=exceptionHandlingService;
    }

    @PostMapping("/{customCustomerId}")
    public ResponseEntity<Map<String,Object>> addQualification( @PathVariable Long customCustomerId ,@Valid @RequestBody Qualification qualification) throws MethodArgumentNotValidException {
        try
        {
            Qualification newQualification= qualificationService.addQualification(customCustomerId,qualification);
            return  ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success",true, "message", "Qualification is added successfully.", "qualification", newQualification));
        }
        catch (EntityAlreadyExistsException exception) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Qualification already exists with examinationName"+ qualification.getExaminationName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (EntityDoesNotExistsException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Customer does not exists with customerId "+ customCustomerId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (ExaminationDoesNotExistsException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Examination does not exists with examinationName "+ qualification.getExaminationName());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    @GetMapping("")
    public ResponseEntity<List<Qualification>> getAllQualifications()
    {
        List<Qualification> qualifications=qualificationService.getAllQualifications();
        return ResponseEntity.status(HttpStatus.OK).body(qualifications);
    }

    @GetMapping("/{customCustomerId}/{qualificationId}")
    public ResponseEntity<Map<String,Object>> getQualificationById(@PathVariable Long customCustomerId,@PathVariable Long qualificationId) throws EntityDoesNotExistsException {
        try
        {
            Qualification qualificationToFind = qualificationService.getQualificationById(customCustomerId,qualificationId);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("success",true, "message", "Qualification is founded.", "qualification", qualificationToFind));
        }
        catch(EntityDoesNotExistsException exception )
        {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Qualification does not exists with id "+ qualificationId+" for customer with id"+ customCustomerId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (CustomerDoesNotExistsException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Customer does not exists with id "+ customCustomerId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/{qualificationId}")
    public ResponseEntity<Map<String,Object>> deleteQualificationById(@PathVariable Long qualificationId) throws EntityDoesNotExistsException {
        try
        {
            Qualification qualificationToDelete = qualificationService.deleteQualification(qualificationId);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("success",true, "message", "Qualification is deleted successfully.", "qualification", qualificationToDelete));
        }
        catch(EntityDoesNotExistsException exception )
        {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Qualification does not exists with id "+ qualificationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/{customCustomerId}/{qualificationId}")
    public ResponseEntity<Map<String,Object>> updateQualificationById(@PathVariable Long customCustomerId,@PathVariable Long qualificationId, @Valid @RequestBody Qualification qualification) throws EntityDoesNotExistsException, MethodArgumentNotValidException {
        try
        {
            Qualification qualificationToUpdate = qualificationService.updateQualification( customCustomerId,qualificationId,qualification);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("success",true, "message", "Qualification is updated successfully.", "qualification", qualificationToUpdate));
        }
        catch(EntityDoesNotExistsException exception )
        {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Qualification does not exists with id "+ qualificationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        catch (EntityAlreadyExistsException exception) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Qualification already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (ExaminationDoesNotExistsException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Examination does not exists with Examination name "+ qualification.getExaminationName());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (CustomerDoesNotExistsException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Customer does not exists with customer id "+ qualification.getExaminationName());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
