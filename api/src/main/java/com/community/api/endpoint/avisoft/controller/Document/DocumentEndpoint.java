package com.community.api.endpoint.avisoft.controller.Document;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.entity.Privileges;
import com.community.api.services.PrivilegeService;
import com.community.api.services.ResponseService;
import com.community.api.services.RoleService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.utils.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping(value = "/document")
public class DocumentEndpoint {
    @Autowired
    private  JwtUtil jwtTokenUtil;

    @Autowired
    private  PrivilegeService privilegeService;

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
/*            String jwtToken = authHeader.substring(7);
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

            if (accessGrant) {*/

                if (documentType.getDescription() == null || documentType.getDocument_type_name() == null) {
                    return responseService.generateErrorResponse("Cannot create Document Type : Fields Empty", HttpStatus.BAD_REQUEST);
                }

                entityManager.persist(documentType);
                return responseService.generateSuccessResponse("Document type created successfully",documentType, HttpStatus.OK);
           /* }else{
                return responseService.generateSuccessResponse("You don't have privilege to create Document ",documentType, HttpStatus.OK);

            }*/

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving Customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-document-types")
    public ResponseEntity<?> getDocumentTypes() {
        try {
            List<DocumentType> documentTypes = entityManager.createQuery("SELECT dt FROM DocumentType dt", DocumentType.class).getResultList();
            return responseService.generateSuccessResponse("Document Types retrieved successfully",documentTypes, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving Document Types", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-document-of-customer")
    public ResponseEntity<?> getDocumentOfCustomer(@RequestParam Long customerId) {
        try {
            List<DocumentType> documentTypes = entityManager.createQuery("SELECT dt FROM Document dt WHERE dt.customCustomer =:customerId", DocumentType.class)
                    .setParameter("customerId", customerId).getResultList();

            if(documentTypes.isEmpty()){
                return responseService.generateResponse(HttpStatus.OK,"No document found",null);
            }
            return responseService.generateSuccessResponse("Document Types retrieved successfully",documentTypes, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving Document Types", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
