package com.community.api.endpoint.avisoft.controller.Document;

import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.utils.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@RestController
@RequestMapping(value = "/document")
public class DocumentEndpoint {
    private EntityManager entityManager;
    private ExceptionHandlingImplement exceptionHandling;
   private ResponseService responseService;
    public DocumentEndpoint(EntityManager entityManager,ExceptionHandlingImplement exceptionHandling,ResponseService responseService)
    {
        this.entityManager=entityManager;
        this.exceptionHandling= exceptionHandling;
        this.responseService=responseService;
    }
    @Transactional
    @RequestMapping(value = "create-document-type", method = RequestMethod.POST)
    public ResponseEntity<?> createDocumentType(@RequestBody DocumentType documentType) {
        try {
            if (documentType.getDocument_type_id() == null || documentType.getDescription() == null) {
                return responseService.generateErrorResponse("Cannot create Document Type : Fields Empty", HttpStatus.BAD_REQUEST);
            }
            entityManager.persist(documentType);
            return responseService.generateSuccessResponse("Document type created successfully",documentType, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
          
            return responseService.generateErrorResponse("Error retrieving Customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
