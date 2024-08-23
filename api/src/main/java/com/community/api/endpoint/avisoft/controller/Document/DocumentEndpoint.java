package com.community.api.endpoint.avisoft.controller.Document;

import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.utils.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@RestController
@RequestMapping(value = "/document")
public class DocumentEndpoint {
    private EntityManager entityManager;
    private ExceptionHandlingImplement exceptionHandling;

    public DocumentEndpoint(EntityManager entityManager,ExceptionHandlingImplement exceptionHandling)
    {
        this.entityManager=entityManager;
        this.exceptionHandling= exceptionHandling;
    }
    @Transactional
    @RequestMapping(value = "create-document-type", method = RequestMethod.POST)
    public ResponseEntity<Object> createDocumentType(@RequestBody DocumentType documentType) {
        try {
            if (documentType.getDocumentTypeId() == null || documentType.getDescription() == null) {
                return new ResponseEntity<>("Cannot create Document Type : Fields Empty", HttpStatus.BAD_REQUEST);
            }
            entityManager.persist(documentType);
            return new ResponseEntity<>(documentType, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error retrieving Customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
