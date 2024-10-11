package com.community.api.endpoint.avisoft.controller.Document;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.Privileges;
import com.community.api.entity.SuccessResponse;
import com.community.api.services.*;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.utils.Document;
import com.community.api.utils.DocumentType;
import com.community.api.utils.ServiceProviderDocument;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/document")
public class DocumentEndpoint {
    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private PrivilegeService privilegeService;

    @Autowired
    private FileService fileService;

    @Autowired
    private DocumentStorageService documentStorageService;

    @Autowired
    private RoleService roleService;
    private EntityManager entityManager;
    private ExceptionHandlingImplement exceptionHandling;
    private ResponseService responseService;

    public DocumentEndpoint(EntityManager entityManager, ExceptionHandlingImplement exceptionHandling, ResponseService responseService) {
        this.entityManager = entityManager;
        this.exceptionHandling = exceptionHandling;
        this.responseService = responseService;
    }

    @Transactional
    @RequestMapping(value = "create-document-type", method = RequestMethod.POST)
    public ResponseEntity<?> createDocumentType(@RequestBody DocumentType documentType, @RequestHeader(value = "Authorization") String authHeader) {
        try {
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();
            boolean accessGrant = false;
            Long userId = null;
            if (role.equals(Constant.SUPER_ADMIN) || role.equals(Constant.ADMIN)) {
                accessGrant = true;

            } else if (role.equals(Constant.SERVICE_PROVIDER)) {
                userId = jwtTokenUtil.extractId(jwtToken);
                List<Privileges> privileges = privilegeService.getServiceProviderPrivilege(userId);

                for (Privileges privilege : privileges) {
                    if (privilege.getPrivilege_name().equals(Constant.PRIVILEGE_ADD_DOCUMENT_TYPE)) {
                        accessGrant = true;
                        break;
                    }
                }
            }

            if (accessGrant) {

                if (documentType.getDescription() == null || documentType.getDocument_type_name() == null) {
                    return responseService.generateErrorResponse("Cannot create Document Type : Fields Empty", HttpStatus.BAD_REQUEST);
                }

                entityManager.persist(documentType);
                return responseService.generateSuccessResponse("Document type created successfully", documentType, HttpStatus.OK);
            } else {
                return responseService.generateSuccessResponse("You don't have privilege to create Document ", documentType, HttpStatus.OK);

            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving Customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-document")
    public ResponseEntity<?> getAllDocuments(@RequestParam(value = "examination", required = false) String exam) {
        try {
            List<DocumentType> documentTypes;

            if (exam != null && !exam.isEmpty()) {
                documentTypes = entityManager.createQuery(

                        "SELECT dt FROM DocumentType dt WHERE dt.description LIKE :exam", DocumentType.class)

                        .setParameter("exam", "%" + "Completed" + "%")
                        .getResultList();
            } else {
                documentTypes = entityManager.createQuery("SELECT dt FROM DocumentType dt WHERE dt.description NOT LIKE :exam", DocumentType.class)
                        .setParameter("exam", "%" + "Completed" + "%")
                        .getResultList();
            }

            if (documentTypes.isEmpty()) {
                return responseService.generateErrorResponse("No document found", HttpStatus.OK);
            }

            return responseService.generateSuccessResponse("Document Types retrieved successfully", documentTypes, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving Document Types", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/get-document-of-customer")
    public ResponseEntity<?> getDocumentOfCustomer(
            @RequestParam Long customerId,
            @RequestParam(required = false) Integer role,
            HttpServletRequest request) {
        try {

            if (role != null) {
                if (roleService.findRoleName(role).equals(Constant.SERVICE_PROVIDER)) {

                    ServiceProviderEntity serviceProviderEntity = entityManager.find(ServiceProviderEntity.class, customerId);

                    if (serviceProviderEntity == null) {
                        return responseService.generateErrorResponse("Data not found", HttpStatus.NOT_FOUND);

                    }
                    StringBuilder jpql = new StringBuilder("SELECT d FROM ServiceProviderDocument d WHERE d.serviceProviderEntity = :serviceProviderEntity");
                    jpql.append(" AND d.filePath != null");
                    TypedQuery<ServiceProviderDocument> query1 = entityManager.createQuery(jpql.toString(), ServiceProviderDocument.class);
                    query1.setParameter("serviceProviderEntity", serviceProviderEntity);
                    List<ServiceProviderDocument> serviceProviderDocuments = query1.getResultList();
                    if (serviceProviderDocuments.isEmpty()) {
                        return responseService.generateSuccessResponse("No documents found",null ,HttpStatus.OK);
                    }
                    List<DocumentResponse> documentResponses = serviceProviderDocuments.stream()
                            .map(serviceProviderDocument -> {
                                String fileName = serviceProviderDocument.getName();
                                String filePath = serviceProviderDocument.getFilePath();
                                String fileUrl = fileService.getFileUrl(filePath, request);
                                String document_name = documentStorageService.findRoleName(serviceProviderDocument.getDocumentType());

                                return new DocumentResponse(fileName, fileUrl, document_name);
                            })
                            .collect(Collectors.toList());
                    return responseService.generateSuccessResponse("Documents retrieved successfully", documentResponses, HttpStatus.OK);
                }

            } else {
                CustomCustomer customer = entityManager.find(CustomCustomer.class, customerId);
                if (customer == null) {
                    return responseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
                }

                StringBuilder jpql = new StringBuilder("SELECT d FROM Document d WHERE d.custom_customer = :customer");
                jpql.append(" AND d.filePath != null");
                TypedQuery<Document> query = entityManager.createQuery(jpql.toString(), Document.class);
                query.setParameter("customer", customer);
                List<Document> documents = query.getResultList();
                if (documents.isEmpty()) {
                    return responseService.generateSuccessResponse("No documents found",null ,HttpStatus.OK);
                }
                List<DocumentResponse> documentResponses = documents.stream()
                        .map(document -> {
                            String fileName = document.getName();
                            String filePath = document.getFilePath();
                            String fileUrl = fileService.getFileUrl(filePath, request);

                            String document_name = documentStorageService.findRoleName(document.getDocumentType());

                            return new DocumentResponse(fileName, fileUrl, document_name);
                        })
                        .collect(Collectors.toList());
                return responseService.generateSuccessResponse("Documents retrieved successfully", documentResponses, HttpStatus.OK);

            }
            return responseService.generateErrorResponse("Invalid request", HttpStatus.BAD_REQUEST);


        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving Documents", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestBody Map<String, Object> loginDetails, HttpServletRequest request, HttpServletResponse response) {
        try {
            String filePath = (String) loginDetails.get("filePath");
            String fileUrl = fileService.getDownloadFileUrl(filePath, request); // No encoding here

            URI uri = URI.create(fileUrl);
            URL url = uri.toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                response.setContentType("application/octet-stream");

                String fileName = url.getPath().substring(url.getPath().lastIndexOf('/') + 1);
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                response.setContentLength(connection.getContentLength());

                try (InputStream inputStream = connection.getInputStream();
                     OutputStream outputStream = response.getOutputStream()) {
                    IOUtils.copy(inputStream, outputStream);
                    outputStream.flush();
                }
            } else {
                return responseService.generateErrorResponse("Error downloading file: " + connection.getResponseMessage(), HttpStatus.BAD_REQUEST);
            }
        } catch (IllegalArgumentException e) {
            return responseService.generateErrorResponse("Invalid file URL: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error downloading file: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error downloading file: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return null;
    }


    private class DocumentResponse {

        private String fileName;
        private String fileUrl;

        private String document_name;


        public DocumentResponse(String fileName, String fileUrl, String document_name) {
            this.fileName = fileName;
            this.fileUrl = fileUrl;
            this.document_name = document_name;

        }

        public String getFileName() {
            return fileName;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public String getDocument_name() {
            return document_name;
        }

    }
}
