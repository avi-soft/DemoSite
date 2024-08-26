package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.entity.ServiceProviderLanguage;
import com.community.api.services.ServiceProviderLanguageService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.twilio.http.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Map;

@RestController
@RequestMapping(value = "/serviceProviderLanguage",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class ServiceProviderLanguageController {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private ServiceProviderLanguageService languageService;
    @Transactional
    @PostMapping("addLanguage")
    private ResponseEntity<?> addLanguage(@RequestBody Map<String,Object> serviceProviderLanguage)
    {
        try{
            return languageService.addLanguage(serviceProviderLanguage);
        }catch (Exception exception)
        {
            exceptionHandling.handleException(exception);
            return new ResponseEntity<>("Error adding language to list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("getLanguages")
    private ResponseEntity<?> getLanguages()
    {
        try{
            return new ResponseEntity<>(languageService.findAllLanguageList(),HttpStatus.OK);
        }catch (Exception exception)
        {
            exceptionHandling.handleException(exception);
            return new ResponseEntity<>("Error adding language to list", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
