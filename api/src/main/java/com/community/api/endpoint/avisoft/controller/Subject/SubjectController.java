package com.community.api.endpoint.avisoft.controller.Subject;

import com.community.api.component.Constant;
import com.community.api.dto.AddStreamDto;
import com.community.api.dto.AddSubjectDto;
import com.community.api.entity.CustomSubject;
import com.community.api.services.ResponseService;
import com.community.api.services.SubjectService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SubjectController {

    private final ExceptionHandlingService exceptionHandlingService;
    private final SubjectService subjectService;

    @Autowired
    public SubjectController(ExceptionHandlingService exceptionHandlingService, SubjectService subjectService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.subjectService = subjectService;
    }

    @PostMapping("/add-subject")
    public ResponseEntity<?> addSubject(@RequestBody AddSubjectDto addSubjectDto, @RequestHeader(value = "Authorization") String authHeader) {
        try{
            if(!subjectService.validiateAuthorization(authHeader)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO ADD A STREAM", HttpStatus.UNAUTHORIZED);
            }

            subjectService.validateAddSubjectDto(addSubjectDto);
            subjectService.saveSubject(addSubjectDto);

            return ResponseService.generateSuccessResponse("SUCCESSFULLY ADDED", addSubjectDto, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-subject")
    public ResponseEntity<?> getAllSubject() {
        try {
            List<CustomSubject> subjectList = subjectService.getAllSubject();
            if (subjectList.isEmpty()) {
                return ResponseService.generateErrorResponse("NO SUBJECT FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("SUBJECTS FOUND", subjectList, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-subject-by-id/{subjectId}")
    public ResponseEntity<?> getSubjectById(@PathVariable Long subjectId) {
        try {
            CustomSubject subject = subjectService.getSubjectBySubjectId(subjectId);
            if (subject == null) {
                return ResponseService.generateErrorResponse("NO SUBJECT FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("SUBJECTS FOUND", subject, HttpStatus.OK);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
