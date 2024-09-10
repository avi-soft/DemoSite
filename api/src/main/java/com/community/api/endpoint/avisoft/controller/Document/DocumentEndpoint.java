package com.community.api.endpoint.avisoft.controller.Document;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.Privileges;
import com.community.api.services.FileService;
import com.community.api.services.PrivilegeService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.utils.Document;
import com.community.api.utils.DocumentType;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/document")
public class DocumentEndpoint {
    @Autowired
    private  JwtUtil jwtTokenUtil;

    @Autowired
    private  PrivilegeService privilegeService;

    @Autowired
    private FileService fileService;

    @Autowired
    private RoleService roleService;
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
                return responseService.generateSuccessResponse("Document type created successfully",documentType, HttpStatus.OK);
            }else{
                return responseService.generateSuccessResponse("You don't have privilege to create Document ",documentType, HttpStatus.OK);

            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving Customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-document")
    public ResponseEntity<?> getAllDocuments() {
        try {
            List<DocumentType> documentTypes = entityManager.createQuery("SELECT dt FROM DocumentType dt", DocumentType.class).getResultList();
            return responseService.generateSuccessResponse("Document Types retrieved successfully",documentTypes, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving Document Types", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-document-of-customer")
    public ResponseEntity<?> getDocumentOfCustomer(@RequestParam Long customerId, HttpServletRequest request) {
        try {
            CustomCustomer customer = entityManager.find(CustomCustomer.class, customerId);
            if (customer == null) {
                return responseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            }
            List<Document> documents = entityManager.createQuery("SELECT d FROM Document d WHERE d.customCustomer = :customer", Document.class)
                    .setParameter("customer", customer)
                    .getResultList();

            if (documents.isEmpty()) {
                return responseService.generateResponse(HttpStatus.OK, "No document found", null);
            }

            List<DocumentResponse> documentResponses = documents.stream()
                    .map(document -> {
                        String fileName = document.getName();
                        String filePath = document.getFilePath();
                        String fileUrl = fileService.getFileUrl(filePath, request);
                        File file = fileService.getFile(filePath);

                        return new DocumentResponse(fileName, fileUrl, file);
                    })
                    .collect(Collectors.toList());
            return responseService.generateSuccessResponse("Documents retrieved successfully", documentResponses, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving Documents", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/download-file")
    public void downloadFile(@RequestParam("filePath") String filePath, HttpServletRequest request, HttpServletResponse response) {
        String fileUrl = fileService.getFileUrl(filePath, request);
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + filePath + "\"");
                IOUtils.copy(connection.getInputStream(), response.getOutputStream());
            } else {
                response.setStatus(responseCode);
            }
        } catch (IOException e) {
            exceptionHandling.handleException(e);
        }
    }

    private class DocumentResponse {

        private String fileName;
        private String fileUrl;
        private File FileUrl;

        public DocumentResponse(String fileName, String fileUrl, File FileUrl) {
            this.fileName = fileName;
            this.fileUrl = fileUrl;
            this.FileUrl = FileUrl;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFileUrl() {
            return fileUrl;
        }
        public File getFileUrl1() {
            return FileUrl;
        }

    }
}
