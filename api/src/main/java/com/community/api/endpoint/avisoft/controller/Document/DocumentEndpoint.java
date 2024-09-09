package com.community.api.endpoint.avisoft.controller.Document;

import com.community.api.dto.DocumentDto;
import com.community.api.services.DocumentService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.EntityAlreadyExistsException;
import com.community.api.services.exception.EntityDoesNotExistsException;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.utils.Document;
import com.community.api.utils.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping(value = "/document")
public class DocumentEndpoint {
    private EntityManager entityManager;
    private ExceptionHandlingImplement exceptionHandling;
   private ResponseService responseService;
   private DocumentService documentService;
    public DocumentEndpoint(EntityManager entityManager,ExceptionHandlingImplement exceptionHandling,ResponseService responseService,DocumentService documentService)
    {
        this.entityManager=entityManager;
        this.exceptionHandling= exceptionHandling;
        this.responseService=responseService;
        this.documentService=documentService;
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

    @GetMapping("/get-all/{customCustomerId}")
    public ResponseEntity<?> getAllDocuments(@PathVariable Long customCustomerId) throws IllegalArgumentException,EntityNotFoundException {
        try {
            List<DocumentDto> documents = documentService.getAllDocumentsWithData(customCustomerId);
            return responseService.generateResponse(HttpStatus.OK, "documents are found", documents);
        }
        catch (EntityNotFoundException e) {
            return ResponseService.generateErrorResponse("",HttpStatus.NOT_FOUND);
        }
        catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse("No documents found for customer with id "+ customCustomerId,HttpStatus.OK);
        }
    }

}
