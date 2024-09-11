package com.community.api.endpoint.avisoft.controller.Customer;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.avisoft.controller.otpmodule.OtpEndpoint;
import com.community.api.endpoint.customer.AddressDTO;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.services.*;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.Document;
import com.community.api.utils.DocumentType;
import com.community.api.utils.ServiceProviderDocument;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.profile.core.domain.Address;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerAddress;
import org.broadleafcommerce.profile.core.service.AddressService;
import org.broadleafcommerce.profile.core.service.CustomerAddressService;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

@RestController
@RequestMapping(value = "/customer",
        produces = {
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_XML_VALUE
        }
)

public class CustomerEndpoint {

    private PasswordEncoder passwordEncoder;
    private CustomerService customerService;  //@TODO- do this task asap
    private ExceptionHandlingImplement exceptionHandling;
    private EntityManager em;
    private CustomCustomerService customCustomerService;
    private AddressService addressService;
    private CustomerAddressService customerAddressService;
    private JwtUtil jwtUtil;


    @Autowired
    private ExceptionHandlingService exceptionHandlingService;

    @Autowired
    private  JwtUtil jwtTokenUtil;

    @Autowired
    private  RoleService roleService;


    @Autowired
    private static ResponseService responseService;

    @Autowired
    private DocumentStorageService documentStorageService;

    @Autowired
    private CatalogService catalogService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setEm(EntityManager em) {
        this.em = em;
    }

    @Autowired
    public void setCustomCustomerService(CustomCustomerService customCustomerService) {
        this.customCustomerService = customCustomerService;
    }

    @Autowired
    public void setAddressService(AddressService addressService) {
        this.addressService = addressService;
    }

    @Autowired
    public void setCustomerAddressService(CustomerAddressService customerAddressService) {
        this.customerAddressService = customerAddressService;
        this.jwtUtil= jwtUtil;
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @RequestMapping(value = "get-customer", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveCustomerById(@RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return responseService.generateErrorResponse("Customer Service Not Initialized",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return responseService.generateErrorResponse("Customer with this ID does not exist" , HttpStatus.NOT_FOUND);

            } else {
                return responseService.generateSuccessResponse("Customer with this ID is found "+customerId,customer , HttpStatus.OK);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving Customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomer(@RequestBody CustomCustomer customerDetails, @RequestParam Long customerId) {

        try {
            if (customerService == null) {
                return responseService.generateErrorResponse("Customer service is not initialized.",HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
            if (customCustomer == null) {
                return responseService.generateErrorResponse("No data found for this customerId",HttpStatus.NOT_FOUND);

            }
            if (customerDetails.getMobileNumber() != null) {
                if (customCustomerService.isValidMobileNumber(customerDetails.getMobileNumber()) == false){
                    return responseService.generateErrorResponse("Cannot update phoneNumber", HttpStatus.INTERNAL_SERVER_ERROR);

                }
            }
            Customer existingCustomerByUsername = null;
            Customer existingCustomerByEmail = null;
            if (customerDetails.getUsername() != null) {
                existingCustomerByUsername = customerService.readCustomerByUsername(customerDetails.getUsername());
            }

            if (customerDetails.getEmailAddress() != null) {
                existingCustomerByEmail = customerService.readCustomerByEmail(customerDetails.getEmailAddress());
            }
            if ((existingCustomerByUsername != null) || existingCustomerByEmail != null) {
                if (existingCustomerByUsername != null && !existingCustomerByUsername.getId().equals(customerId)) {
                    return responseService.generateErrorResponse("Cannot update phoneNumber", HttpStatus.INTERNAL_SERVER_ERROR);

                }
                if (existingCustomerByEmail != null && !existingCustomerByEmail.getId().equals(customerId)) {
                    return responseService.generateErrorResponse("Email not available", HttpStatus.BAD_REQUEST);
                }
            }
            customerDetails.setId(customerId);
            customerDetails.setMobileNumber(customCustomer.getMobileNumber());
            customerDetails.setQualificationDetailsList(customCustomer.getQualificationDetailsList());

            customerDetails.setCountryCode(customCustomer.getCountryCode());
            Customer customer = customerService.readCustomerById(customerId);
            //using reflections
            for (Field field : CustomCustomer.class.getDeclaredFields()) {
                field.setAccessible(true);
                Object newValue = field.get(customerDetails);
                if (newValue != null) {
                    field.set(customCustomer, newValue);
                }
            }
            if (customerDetails.getFirstName() != null || customerDetails.getLastName() != null) {
                customer.setFirstName(customerDetails.getFirstName());
                customer.setLastName(customerDetails.getLastName());
            }
            if(customerDetails.getEmailAddress()!=null){
                customer.setEmailAddress(customerDetails.getEmailAddress());
            }

            em.merge(customCustomer);
            return responseService.generateSuccessResponse("User details updated successfully : ",customer, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @PostMapping("/upload-document")
    public ResponseEntity<?> updateDocument(
            @RequestParam Long customerId,
            @RequestPart(value = "Aadhaar Card", required = false) MultipartFile aadharCard,
            @RequestPart(value = "PAN Card", required = false) MultipartFile panCard,
            @RequestPart(value = "Passport Size Photo", required = false) MultipartFile photo) {
        try {
            if (customerService == null) {
                return responseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
            if (customCustomer == null) {
                return responseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);
            }

            Map<String, Object> responseData = new HashMap<>();
            Map<String, MultipartFile> files = new HashMap<>();

            if (aadharCard != null) {
                files.put("Aadhar Card", aadharCard);
            }
            if (panCard != null) {
                files.put("PAN Card", panCard);
            }
            if (photo != null) {
                files.put("Passport Size Photo", photo);
            }

            for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
                String documentType = entry.getKey();
                MultipartFile file = entry.getValue();

                ResponseEntity<Map<String, Object>> savedResponse = documentStorageService.saveDocuments(file, documentType, customerId, "customer");
                Map<String, Object> responseBody = savedResponse.getBody();

                if (savedResponse.getStatusCode() != HttpStatus.OK) {
                    return responseService.generateErrorResponse("Error uploading " + documentType, HttpStatus.INTERNAL_SERVER_ERROR);
                }

                System.out.println(documentType.trim() + " documentType.trim()");
                // Find or create DocumentType
                DocumentType documentTypeObj = em.createQuery(
                                "SELECT dt FROM DocumentType dt WHERE dt.document_type_name = :documentTypeName", DocumentType.class)
                        .setParameter("documentTypeName", documentType.trim())
                        .getResultStream()
                        .findFirst()
                        .orElseGet(null);

                String fileName = file.getOriginalFilename();
                Document doc = new Document();
                doc.setName(fileName);
                String filePath = "avisoft"
                        + File.separator
                        + "customer"
                        + File.separator
                        + customerId
                        + File.separator
                        + documentType
                        + File.separator
                        + fileName;
                doc.setFilePath(filePath);
//                doc.setData(file.getBytes());
                doc.setCustom_customer(customCustomer);
                doc.setDocumentType(documentTypeObj);
                em.persist(doc);

                responseData.put(documentType, responseBody.get("data"));
            }

            return responseService.generateSuccessResponse("Documents uploaded successfully", responseData, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating documents", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @PostMapping("/upload-documents")
    public ResponseEntity<?> updateDocuments(
            @RequestParam Long customerId,
            @RequestParam Map<String, MultipartFile> files,
            @RequestHeader(value = "Authorization") String authHeader) {
        try {

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return responseService.generateErrorResponse("Authorization header is missing or invalid.", HttpStatus.UNAUTHORIZED);
            }

            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();

            if (!customerId.equals(tokenUserId)) {
                return responseService.generateErrorResponse("Unauthorized request.", HttpStatus.UNAUTHORIZED);
            }

            if(files.size()==0){
                return ResponseService.generateErrorResponse("Invalid request.", HttpStatus.BAD_REQUEST);

            }


            if (roleService.findRoleName(roleId).equals(Constant.roleUser)) {
                CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
                if (customCustomer == null) {
                    return responseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);
                }

                Map<String, Object> responseData = new HashMap<>();
                List<String> deletedDocumentMessages = new ArrayList<>();


                for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
                    Integer fileNameId = Integer.parseInt(entry.getKey());
                    MultipartFile file = entry.getValue();
                    System.out.println(file.getContentType() + "fregf" );
                    if (!DocumentStorageService.isValidFileType(file)) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "status", ApiConstants.STATUS_ERROR,
                                "status_code", HttpStatus.BAD_REQUEST.value(),
                                "message", "Invalid file type: "
                        ));
                    }

                    DocumentType documentTypeObj = em.createQuery(
                                    "SELECT dt FROM DocumentType dt WHERE dt.document_type_id = :documentTypeId", DocumentType.class)
                            .setParameter("documentTypeId", fileNameId)
                            .getResultStream()
                            .findFirst()
                            .orElse(null);

                    if (documentTypeObj == null) {
                        return responseService.generateErrorResponse("Unknown document type for file: " + fileNameId, HttpStatus.BAD_REQUEST);
                    }

                    Document existingDocument = em.createQuery(
                                    "SELECT d FROM Document d WHERE d.custom_customer = :customCustomer AND d.documentType = :documentType", Document.class)
                            .setParameter("customCustomer", customCustomer)
                            .setParameter("documentType", documentTypeObj)
                            .getResultStream()
                            .findFirst()
                            .orElse(null);

                    System.out.println(file.getContentType() + " file");
                    if ((file.isEmpty() || file ==null) && existingDocument!=null) {
                        if (existingDocument != null) {
                            String filePath = existingDocument.getFilePath();
                            System.out.println(filePath + " filePath");
                            if (filePath != null) {
                                File filesobj = new File(filePath);
                                if (filesobj.exists()) {
                                    filesobj.delete();
                                }
                            }

                               existingDocument.setDocumentType(null);
                               existingDocument.setFilePath(null);
                               existingDocument.setName(null);


                            deletedDocumentMessages.add("File for document type '" + documentTypeObj.getDocument_type_name() + "' has been deleted.");
                        }
                        continue;
                    }

                    // If the file is not empty and a document already exists, update the document
                    if (existingDocument != null && (!file.isEmpty() || file !=null)) {
                        String filePath = existingDocument.getFilePath();
                        if (filePath != null) {
                            File oldFile = new File(filePath);
                            String oldFileName = oldFile.getName();
                            String newFileName = file.getOriginalFilename();

                            if (!newFileName.equals(oldFileName)) {
                                oldFile.delete();
                                documentStorageService.updateOrCreateDocument(existingDocument, file, documentTypeObj, customerId, role);
                            }
                        }
                    } else {
                        // If the file is not empty create the document
                        if(!file.isEmpty() || file !=null){
                            documentStorageService.createDocument(file, documentTypeObj, customCustomer, customerId, role);
                        }
                    }

                    ResponseEntity<Map<String, Object>> savedResponse = documentStorageService.saveDocuments(file, documentTypeObj.getDocument_type_name(), customerId, role);
                    Map<String, Object> responseBody = savedResponse.getBody();
                    if (!deletedDocumentMessages.isEmpty()) {
                        responseData.put("deletedMessages", deletedDocumentMessages);
                    } else if (!file.isEmpty() || file !=null && savedResponse.getStatusCode() != HttpStatus.OK ) {
                        String status = (String) responseBody.get("status");
                        HttpStatus httpStatus = HttpStatus.valueOf((Integer) responseBody.get("status_code"));
                        return responseService.generateErrorResponse((String) responseBody.get("message"), httpStatus);
                    }

                    responseData.put(documentTypeObj.getDocument_type_name(), responseBody.get("data"));
                }

            }else{
                ServiceProviderEntity serviceProviderEntity = em.find(ServiceProviderEntity.class, customerId);
                if (serviceProviderEntity == null) {
                    return responseService.generateErrorResponse("No data found for this serviceProvider", HttpStatus.NOT_FOUND);
                }

                Map<String, Object> responseData = new HashMap<>();
                List<String> deletedDocumentMessages = new ArrayList<>();

                // Handle file uploads and deletions
                for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
                    Integer fileNameId = Integer.parseInt(entry.getKey());
                    MultipartFile file = entry.getValue();

                    DocumentType documentTypeObj = em.createQuery(
                                    "SELECT dt FROM DocumentType dt WHERE dt.document_type_id = :documentTypeId", DocumentType.class)
                            .setParameter("documentTypeId", fileNameId)
                            .getResultStream()
                            .findFirst()
                            .orElse(null);

                    if (documentTypeObj == null) {
                        return responseService.generateErrorResponse("Unknown document type for file: " + fileNameId, HttpStatus.BAD_REQUEST);
                    }

                    ServiceProviderDocument existingDocument = em.createQuery(
                                    "SELECT d FROM ServiceProviderDocument d WHERE d.serviceProviderEntity = :serviceProviderEntity AND d.documentType = :documentType", ServiceProviderDocument.class)
                            .setParameter("serviceProviderEntity", serviceProviderEntity)
                            .setParameter("documentType", documentTypeObj)
                            .getResultStream()
                            .findFirst()
                            .orElse(null);

                    if ((file.isEmpty() || file ==null) && existingDocument!=null) {
                        if (existingDocument != null) {

                            String filePath = existingDocument.getFilePath();
                            if (filePath != null) {
                                File filesobj = new File(filePath);
                                if (filesobj.exists()) {
                                    filesobj.delete();
                                }
                            }
                            existingDocument.setDocumentType(null);
                        existingDocument.setName(null);
                        existingDocument.setFilePath(null);
                        em.persist(existingDocument);

                            deletedDocumentMessages.add("File for document type '" + documentTypeObj.getDocument_type_name() + "' has been deleted.");
                        }
                        continue;
                    }

                    // If the file is not empty and a document already exists, update the document
                    if (existingDocument != null && (!file.isEmpty() || file !=null)) {
                        String filePath = existingDocument.getFilePath();
                        if (filePath != null) {
                            File oldFile = new File(filePath);
                            String oldFileName = oldFile.getName();
                            String newFileName = file.getOriginalFilename();

                            if (!newFileName.equals(oldFileName)) {
                                oldFile.delete();
                                documentStorageService.updateOrCreateServiceProvider(existingDocument, file, documentTypeObj, customerId, role);
                            }
                        }
                    } else {
                        // If the file is not empty create the document
                        if(!file.isEmpty() || file !=null){
                            documentStorageService.createDocumentServiceProvider(file, documentTypeObj, serviceProviderEntity, customerId, role);
                        }
                    }

                    ResponseEntity<Map<String, Object>> savedResponse = documentStorageService.saveDocuments(file, documentTypeObj.getDocument_type_name(), customerId, role);
                    Map<String, Object> responseBody = savedResponse.getBody();
                    if (!deletedDocumentMessages.isEmpty()) {
                        responseData.put("deletedMessages", deletedDocumentMessages);
                    } else if (!file.isEmpty() || file !=null && savedResponse.getStatusCode() != HttpStatus.OK ) {
                        String status = (String) responseBody.get("status");
                        HttpStatus httpStatus = HttpStatus.valueOf((Integer) responseBody.get("status_code"));
                        return responseService.generateErrorResponse((String) responseBody.get("message"), httpStatus);
                    }

                    responseData.put(documentTypeObj.getDocument_type_name(), responseBody.get("data"));
                }
                return responseService.generateSuccessResponse("Documents updated successfully", responseData, HttpStatus.OK);
            }


            return responseService.generateSuccessResponse("Invalid request", null, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating documents: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @Transactional
    @RequestMapping(value = "update-username", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerUsername(@RequestBody Map<String, Object> updates, @RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return responseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            String username = (String) updates.get("username");
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return responseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);

            }
            Customer existingCustomerByUsername = null;
            if (username != null) {
                existingCustomerByUsername = customerService.readCustomerByUsername(username);
            } else{
                return responseService.generateErrorResponse("username Empty", HttpStatus.BAD_REQUEST);

            }

            if ((existingCustomerByUsername != null) && !existingCustomerByUsername.getId().equals(customerId)) {
                return responseService.generateErrorResponse("Username is not available", HttpStatus.BAD_REQUEST);

            } else {
                customer.setUsername(username);
                em.merge(customer);
                return responseService.generateSuccessResponse("User name  updated successfully : ",customer, HttpStatus.OK);

            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Error updating username", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @RequestMapping(value = "create-or-update-password", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerPassword(@RequestBody Map<String,Object>details, @RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return responseService.generateErrorResponse("Customer service is not initialized.",HttpStatus.INTERNAL_SERVER_ERROR);

            }
            String password=(String) details.get("password");
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return responseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);
            }
            if(password!=null) {
                if (customer.getPassword() == null || customer.getPassword().isEmpty()) {
                    customer.setPassword(passwordEncoder.encode(password));
                    em.merge(customer);
                    return responseService.generateSuccessResponse("Password Created", customer, HttpStatus.OK);
                }
                if (!passwordEncoder.matches(password, customer.getPassword())) {
            /*if (customerDTO.getPassword() != null && customerDTO.getOldPassword() != null) {
                if (passwordEncoder.matches(customerDTO.getOldPassword(), customer.getPassword())) {
                    if (!customerDTO.getPassword().equals(customerDTO.getOldPassword())) {*/
                    customer.setPassword(passwordEncoder.encode(password));
                    em.merge(customer);
                    return responseService.generateSuccessResponse("Password Updated", customer, HttpStatus.OK);
                    /*} else
                        return new ResponseEntity<>("Old password and new password can not be same!", HttpStatus.BAD_REQUEST);
                } else
                    return new ResponseEntity<>("The old password you provided is incorrect. Please try again with the correct old password", HttpStatus.BAD_REQUEST);
            }*/
                }
                return responseService.generateErrorResponse("Old Password and new Password cannot be same",HttpStatus.BAD_REQUEST);
            }else {
                return responseService.generateErrorResponse("Empty Password", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Error updating password", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "delete", method = RequestMethod.DELETE)
    public ResponseEntity<?> updateCustomer(@RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return responseService.generateErrorResponse(ApiConstants.CUSTOMER_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);

            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer != null) {
                customerService.deleteCustomer(customerService.readCustomerById(customerId));
                return responseService.generateSuccessResponse("Record Deleted Successfully","", HttpStatus.OK);

            } else {
                return responseService.generateErrorResponse("No Records found for this ID " + customerId, HttpStatus.NOT_FOUND);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some issue in deleting customer " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);


        }
    }

    @Transactional
    @RequestMapping(value = "add-address", method = RequestMethod.POST)
    public ResponseEntity<?> addAddress(@RequestParam Long customerId, @RequestBody Map<String, Object> addressDetails) {
        try {
            if (customerService == null) {
                return responseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Customer customer = customerService.readCustomerById(customerId);
            if (customer != null) {
                CustomerAddress newAddress = customerAddressService.create();
                Address address = addressService.create();
                address.setAddressLine1((String) addressDetails.get("address"));
                address.setCity((String) addressDetails.get("city"));
                address.setStateProvinceRegion((String) addressDetails.get("state"));
                address.setCounty((String) addressDetails.get("district"));
                address.setPostalCode((String) addressDetails.get("pinCode"));
                newAddress.setAddress(address);
                newAddress.setCustomer(customer);
                newAddress.setAddressName((String) addressDetails.get("addressName"));
                List<CustomerAddress> addressLists = customer.getCustomerAddresses();
                addressLists.add(newAddress);
                customer.setCustomerAddresses(addressLists);
                em.merge(customer);

                //using reflections
                AddressDTO addressDTO = new AddressDTO();
                for (Map.Entry<String, Object> entry : addressDetails.entrySet()) {
                    try {
                        Field field = AddressDTO.class.getDeclaredField(entry.getKey());
                        field.setAccessible(true);
                        field.set(addressDTO, entry.getValue());
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        exceptionHandling.handleException(e);
                    }
                }
                addressDTO.setDistrict(address.getCounty());
                addressDTO.setCustomerId(newAddress.getCustomer().getId());
                CustomCustomer customCustomer = em.find(CustomCustomer.class, newAddress.getCustomer().getId());
                if (customCustomer == null) {
                    return responseService.generateErrorResponse("Error saving address", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                addressDTO.setPhoneNumber(customCustomer.getMobileNumber());
                return responseService.generateSuccessResponse("Address added successfully : ",addressDTO, HttpStatus.OK);


            } else {
                return responseService.generateErrorResponse("No Records found for this ID", HttpStatus.NOT_FOUND);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error saving Address", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @RequestMapping(value = "retrieve-address", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveAddressList(@RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return responseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            Customer customer = customerService.readCustomerById(customerId);
            if(customer!=null){
                List<CustomerAddress>addressList=customer.getCustomerAddresses();
                List<AddressDTO>listOfAddresses=new ArrayList<>();
                for(CustomerAddress customerAddress:addressList)
                {
                    AddressDTO addressDTO=makeAddressDTO(customerAddress);
                    listOfAddresses.add(addressDTO);
                }
                return responseService.generateSuccessResponse("Addresses details : ",listOfAddresses, HttpStatus.OK);
            }else{
                return responseService.generateErrorResponse("No data found for this customerId", HttpStatus.INTERNAL_SERVER_ERROR);

            }


        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error in retreiving Address", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
    @Transactional
    @RequestMapping(value = "address-details", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveAddressList(@RequestParam Long customerId,@RequestParam Long addressId) {
        try {
            if (customerService == null) {
                return responseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            Customer customer = customerService.readCustomerById(customerId);
            CustomerAddress customerAddress=customerAddressService.readCustomerAddressById(addressId);
            if(customerAddress==null)
            {
                return responseService.generateErrorResponse("Address not found",HttpStatus.NOT_FOUND);
            }
            else{
                return responseService.generateSuccessResponse("Address details : ", makeAddressDTO(customerAddress), HttpStatus.OK);

            }
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error saving Address", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
    public AddressDTO makeAddressDTO(CustomerAddress customerAddress)
    {
        AddressDTO addressDTO=new AddressDTO();
        addressDTO.setAddress(customerAddress.getAddress().getAddressLine1());
        addressDTO.setPinCode(customerAddress.getAddress().getPostalCode());
        addressDTO.setState(customerAddress.getAddress().getStateProvinceRegion());
        addressDTO.setCity(customerAddress.getAddress().getCity());
        addressDTO.setCustomerId(customerAddress.getCustomer().getId());
        addressDTO.setAddressName(customerAddress.getAddressName());
        CustomCustomer customCustomer=em.find(CustomCustomer.class,customerAddress.getCustomer().getId());
        addressDTO.setPhoneNumber(customCustomer.getMobileNumber());
        return addressDTO;
    }
/*
    public static ResponseEntity<?> createAuthResponse(String token, Customer customer ) {
        OtpEndpoint.ApiResponse authResponse = new OtpEndpoint.ApiResponse(token, customer, HttpStatus.OK.value(), HttpStatus.OK.name(),"User has been logged in");
        return responseService.generateSuccessResponse("Token details : ", authResponse, HttpStatus.OK);
    }
*/



    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("Token is required");
        }
        try {
            jwtUtil.logoutUser(token);

            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during logout");
        }
    }

//    @GetMapping(value = "/savedForms/getProductsByUserId")
//    public ResponseEntity<?> getSavedFormsByUserId(HttpServletRequest request,@RequestParam(value = "id") String id) throws Exception{
//        try {
//            if (catalogService == null) {
//                return new ResponseEntity<>("catalogService is null", HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//
//            Long categoryId = Long.parseLong(id);
//            if(categoryId <= 0){
//                return new ResponseEntity<>("CATEGORYCANNOTBELESSTHANOREQAULZERO", HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//
//            Category category = this.catalogService.findCategoryById(categoryId);
//
//            if (category == null) {
//                return new ResponseEntity<>("Category not Found", HttpStatus.INTERNAL_SERVER_ERROR);
//            } else if (((Status) category).getArchived() == 'Y') {
//                return new ResponseEntity<>("Category is Archived", HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//
//            List<BigInteger> productIdList = categoryService.getAllProductsByCategoryId(categoryId);
//            List<CustomProductWrapper> products = new ArrayList<>();
//
//            for (BigInteger productId : productIdList) {
//                CustomProduct customProduct = entityManager.find(CustomProduct.class, productId.longValue());
//
//                if(customProduct != null && (((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {
//                    CustomProductWrapper wrapper = new CustomProductWrapper();
//                    wrapper.wrapDetails(customProduct);
//                    products.add(wrapper);
//                }
//            }
//
//            AddCategoryDto categoryDao = new AddCategoryDto();
//            categoryDao.setCategoryId(category.getId());
//            categoryDao.setCategoryName(category.getName());
//            categoryDao.setProducts(products);
//            categoryDao.setTotalProducts(Long.valueOf(products.size()));
//
//            return ResponseEntity.status(HttpStatus.OK).body(categoryDao);
//
//        } catch (Exception exception) {
//            exceptionHandlingService.handleException(exception);
//            return new ResponseEntity<>("SOMEEXCEPTIONOCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//    @GetMapping(value = "/recommendations/getProductsByUserId")
//    public ResponseEntity<?> getRecommendationsBYUserId(HttpServletRequest request,@RequestParam(value = "id") String id) throws Exception{
//        try {
//            if (catalogService == null) {
//                return new ResponseEntity<>("catalogService is null", HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//
//            Long categoryId = Long.parseLong(id);
//            if(categoryId <= 0){
//                return new ResponseEntity<>("CATEGORYCANNOTBELESSTHANOREQAULZERO", HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//
//            Category category = this.catalogService.findCategoryById(categoryId);
//
//            if (category == null) {
//                return new ResponseEntity<>("Category not Found", HttpStatus.INTERNAL_SERVER_ERROR);
//            } else if (((Status) category).getArchived() == 'Y') {
//                return new ResponseEntity<>("Category is Archived", HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//
//            List<BigInteger> productIdList = categoryService.getAllProductsByCategoryId(categoryId);
//            List<CustomProductWrapper> products = new ArrayList<>();
//
//            for (BigInteger productId : productIdList) {
//                CustomProduct customProduct = entityManager.find(CustomProduct.class, productId.longValue());
//
//                if(customProduct != null && (((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {
//                    CustomProductWrapper wrapper = new CustomProductWrapper();
//                    wrapper.wrapDetails(customProduct);
//                    products.add(wrapper);
//                }
//            }
//
//            AddCategoryDto categoryDao = new AddCategoryDto();
//            categoryDao.setCategoryId(category.getId());
//            categoryDao.setCategoryName(category.getName());
//            categoryDao.setProducts(products);
//            categoryDao.setTotalProducts(Long.valueOf(products.size()));
//
//            return ResponseEntity.status(HttpStatus.OK).body(categoryDao);
//
//        } catch (Exception exception) {
//            exceptionHandlingService.handleException(exception);
//            return new ResponseEntity<>("SOMEEXCEPTIONOCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @GetMapping(value = "/filledForms/getProductsByUserId")
//    public ResponseEntity<?> getFilledFormsByUserId(HttpServletRequest request,@RequestParam(value = "id") String id) throws Exception{
//        try {
//            if (catalogService == null) {
//                return new ResponseEntity<>("catalogService is null", HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//
//            Long categoryId = Long.parseLong(id);
//            if(categoryId <= 0){
//                return new ResponseEntity<>("CATEGORYCANNOTBELESSTHANOREQAULZERO", HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//
//            Category category = this.catalogService.findCategoryById(categoryId);
//
//            if (category == null) {
//                return new ResponseEntity<>("Category not Found", HttpStatus.INTERNAL_SERVER_ERROR);
//            } else if (((Status) category).getArchived() == 'Y') {
//                return new ResponseEntity<>("Category is Archived", HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//
//            List<BigInteger> productIdList = categoryService.getAllProductsByCategoryId(categoryId);
//            List<CustomProductWrapper> products = new ArrayList<>();
//
//            for (BigInteger productId : productIdList) {
//                CustomProduct customProduct = entityManager.find(CustomProduct.class, productId.longValue());
//
//                if(customProduct != null && (((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {
//                    CustomProductWrapper wrapper = new CustomProductWrapper();
//                    wrapper.wrapDetails(customProduct);
//                    products.add(wrapper);
//                }
//            }
//
//            AddCategoryDto categoryDao = new AddCategoryDto();
//            categoryDao.setCategoryId(category.getId());
//            categoryDao.setCategoryName(category.getName());
//            categoryDao.setProducts(products);
//            categoryDao.setTotalProducts(Long.valueOf(products.size()));
//
//            return ResponseEntity.status(HttpStatus.OK).body(categoryDao);
//
//        } catch (Exception exception) {
//            exceptionHandlingService.handleException(exception);
//            return new ResponseEntity<>("SOMEEXCEPTIONOCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

}