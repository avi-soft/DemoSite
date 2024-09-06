package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.entity.ServiceProviderLanguage;
import com.community.api.services.ResponseService;
import com.community.api.services.SanitizerService;
import com.community.api.services.ServiceProviderLanguageService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.twilio.http.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Map;

@RestController
@RequestMapping(value = "/service-provider-language",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class ServiceProviderLanguageController {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private ServiceProviderLanguageService languageService;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    @Autowired
    private SanitizerService sanitizerService;
    @Transactional
    @PostMapping("add-language")
    private ResponseEntity<?> addLanguage(@RequestBody Map<String,Object> serviceProviderLanguage)
    {
        try{
            if(!sharedUtilityService.validateInputMap(serviceProviderLanguage).equals(SharedUtilityService.ValidationResult.SUCCESS))
            {
                return ResponseService.generateErrorResponse("Invalid Request Body",HttpStatus.UNPROCESSABLE_ENTITY);
            }
            serviceProviderLanguage=sanitizerService.sanitizeInputMap(serviceProviderLanguage);
            return languageService.addLanguage(serviceProviderLanguage);
        }catch (Exception exception)
        {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Error adding language to list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("get-languages")
    private ResponseEntity<?> getLanguages()
    {
        try{
            return responseService.generateSuccessResponse("List Fetched Successfully",languageService.findAllLanguageList(),HttpStatus.OK);
        }catch (Exception exception)
        {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Error adding language to list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
