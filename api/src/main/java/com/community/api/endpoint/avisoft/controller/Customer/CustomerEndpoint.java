package com.community.api.endpoint.avisoft.controller.Customer;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.avisoft.controller.otpmodule.OtpEndpoint;
import com.community.api.endpoint.customer.AddressDTO;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.Qualification;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.QualificationDetails;
import com.community.api.services.*;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.Document;
import com.community.api.utils.DocumentType;
import com.community.api.utils.ServiceProviderDocument;
import io.micrometer.core.lang.Nullable;
import org.apache.commons.fileupload.FileUploadException;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.profile.core.domain.Address;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerAddress;
import org.broadleafcommerce.profile.core.domain.CustomerImpl;
import org.broadleafcommerce.profile.core.service.AddressService;
import org.broadleafcommerce.profile.core.service.CustomerAddressService;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.constraints.Size;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

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
    private DocumentStorageService fileUploadService;

    @Autowired
    private static SharedUtilityService sharedUtilityServiceApi;

    @Autowired
    private ExceptionHandlingService exceptionHandlingService;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private RoleService roleService;

    @Autowired
    private DistrictService districtService;

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
    private SharedUtilityService sharedUtilityService;

    @Autowired
    public void setAddressService(AddressService addressService) {
        this.addressService = addressService;
    }

    @Autowired
    public void setCustomerAddressService(CustomerAddressService customerAddressService) {
        this.customerAddressService = customerAddressService;
        this.jwtUtil = jwtUtil;
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @RequestMapping(value = "get-customer", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveCustomerById(@RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer Service Not Initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer with this ID does not exist", HttpStatus.NOT_FOUND);

            } else {
                return ResponseService.generateSuccessResponse("Customer with this ID is found " + customerId, customer, HttpStatus.OK);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving Customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomer(@RequestBody Map<String, Object> details, @RequestParam Long customerId) {
        try {
            List<String> errorMessages = new ArrayList<>();
            Iterator<String> iterator = details.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (details.get(key).toString().isEmpty()) {
                    iterator.remove(); // Safely remove using the iterator
                    errorMessages.add(key + " cannot be null");
                }
            }
            if (!errorMessages.isEmpty()) {
                return ResponseService.generateErrorResponse("List of Failed validations: " + errorMessages.toString(), HttpStatus.BAD_REQUEST);
            }
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
            if (customCustomer == null) {
                return ResponseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);
            }

            // Validate mobile number
            String mobileNumber = (String) details.get("mobileNumber");
            if (mobileNumber != null && !customCustomerService.isValidMobileNumber(mobileNumber)) {
                return ResponseService.generateErrorResponse("Cannot update phoneNumber", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Check for existing username and email
            String username = (String) details.get("username");
            String emailAddress = (String) details.get("emailAddress");
            Customer existingCustomerByUsername = (username != null) ? customerService.readCustomerByUsername(username) : null;
            Customer existingCustomerByEmail = (emailAddress != null) ? customerService.readCustomerByEmail(emailAddress) : null;

            if ((existingCustomerByUsername != null && !existingCustomerByUsername.getId().equals(customerId)) ||
                    (existingCustomerByEmail != null && !existingCustomerByEmail.getId().equals(customerId))) {
                return ResponseService.generateErrorResponse("Email or Username already in use", HttpStatus.BAD_REQUEST);
            }

            // Update customer fields
            customCustomer.setId(customerId);
            customCustomer.setMobileNumber(customCustomer.getMobileNumber());
            customCustomer.setQualificationDetailsList(customCustomer.getQualificationDetailsList());
            customCustomer.setCountryCode(customCustomer.getCountryCode());


            if (details.containsKey("firstName")&&!details.get("firstName").toString().isEmpty()) {
                customCustomer.setFirstName((String) details.get("firstName"));
            } else if (details.containsKey("firstName")&&details.get("firstName").toString().isEmpty())
            {
                errorMessages.add("First name cannot be null");
            }
            if (details.containsKey("lastName")&&!details.get("lastName").toString().isEmpty())
                customCustomer.setLastName((String) details.get("lastName"));
            else if (details.containsKey("lastName")&&details.get("lastName").toString().isEmpty())
            {
                errorMessages.add("Last name cannot be null");
            }



            if (details.containsKey("emailAddress") && ((String) details.get("emailAddress")).isEmpty())
                errorMessages.add("email Address cannot be null");
            if (details.containsKey("emailAddress") && !((String) details.get("emailAddress")).isEmpty())
                customCustomer.setEmailAddress(emailAddress);
            // Handle dynamic fields
            details.remove("firstName");
            details.remove("lastName");
            details.remove("emailAddress");
            String state = (String) details.get("currentState");
            String district = (String) details.get("currentDistrict");
            String pincode = (String) details.get("currentPincode");
            if (state != null && district != null && pincode != null) {
                boolean updated=false;
                for (CustomerAddress customerAddress : customCustomer.getCustomerAddresses()) {
                    if (customerAddress.getAddressName().equals("CURRENT_ADDRESS")) {
                        customerAddress.getAddress().setAddressLine1((String) details.get("currentAddress"));
                        customerAddress.getAddress().setStateProvinceRegion(districtService.findStateById(Integer.parseInt(state)));
                        customerAddress.getAddress().setCounty(districtService.findDistrictById(Integer.parseInt(district)));
                        customerAddress.getAddress().setPostalCode(pincode);
                        customerAddress.getAddress().setCity((String) details.get("currentCity"));
                        updated = true;
                        break;
                    }
                }
                if(!updated) {
                    Map<String, Object> addressMap = new HashMap<>();
                    addressMap.put("address", details.get("currentAddress"));
                    addressMap.put("state", districtService.findStateById(Integer.parseInt(state)));
                    addressMap.put("city", details.get("currentCity"));
                    addressMap.put("district", districtService.findDistrictById(Integer.parseInt(district)));
                    addressMap.put("pinCode", pincode);
                    addressMap.put("addressName", "CURRENT_ADDRESS");
                    addAddress(customerId, addressMap);
                }
            }
            details.remove("currentState");
            details.remove("currentDistrict");
            details.remove("currentAddress");
            details.remove("currentPincode");
            details.remove("currentCity");
            state = (String) details.get("permanentState");
            district = (String) details.get("permanentDistrict");
            pincode = (String) details.get("permanentPincode");
            if (state != null && district != null && pincode != null) {
                boolean updated = false;
                for (CustomerAddress customerAddress : customCustomer.getCustomerAddresses()) {

                    if (customerAddress.getAddressName().equals("PERMANENT_ADDRESS")) {
                        System.out.println("1");
                        customerAddress.getAddress().setAddressLine1((String) details.get("permanentAddress"));
                        customerAddress.getAddress().setStateProvinceRegion(districtService.findStateById(Integer.parseInt(state)));
                        customerAddress.getAddress().setCounty(districtService.findDistrictById(Integer.parseInt(district)));
                        customerAddress.getAddress().setPostalCode(pincode);
                        customerAddress.getAddress().setCity((String) details.get("permanentCity"));
                        updated = true;
                        break;
                    }
                }
                if (!updated) {
                    Map<String, Object> addressMap = new HashMap<>();
                    addressMap.put("address", details.get("permanentAddress"));
                    addressMap.put("state", districtService.findStateById(Integer.parseInt(state)));
                    addressMap.put("city", details.get("permanentCity"));
                    addressMap.put("district", districtService.findDistrictById(Integer.parseInt(district)));
                    addressMap.put("pinCode", pincode);
                    addressMap.put("addressName", "PERMANENT_ADDRESS");
                    addAddress(customerId, addressMap);
                }
            }
            details.remove("permanentState");
            details.remove("permanentDistrict");
            details.remove("permanentAddress");
            details.remove("permanentPincode");
            details.remove("permanentCity");

            for (Map.Entry<String, Object> entry : details.entrySet()) {
                String fieldName = entry.getKey();
                Object newValue = entry.getValue();
                Field field = CustomCustomer.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                Column columnAnnotation = field.getAnnotation(Column.class);
                boolean isColumnNotNull = (columnAnnotation != null && !columnAnnotation.nullable());
                // Check if the field has the @Nullable annotation
                boolean isNullable = field.isAnnotationPresent(Nullable.class);
                field.setAccessible(true);
                if (newValue.toString().isEmpty() && !isNullable) {
                    errorMessages.add(fieldName + " cannot be null");
                    continue;
                }
                // Validate not null

                // Validate size if applicable
                if (field.isAnnotationPresent(Size.class)) {
                    Size sizeAnnotation = field.getAnnotation(Size.class);
                    int min = sizeAnnotation.min();
                    int max = sizeAnnotation.max();
                    if (newValue.toString().length() > max || newValue.toString().length() < min) {
                        errorMessages.add(fieldName + " size should be between " + min + " and " + max);
                        continue;
                    }
                }

                // Set value if type is compatible
                if (newValue != null && field.getType().isAssignableFrom(newValue.getClass())) {
                    field.set(customCustomer, newValue);
                }
            }

            // Update address if needed


            if (!errorMessages.isEmpty()) {
                return ResponseService.generateErrorResponse("List of Failed validations: " + errorMessages.toString(), HttpStatus.BAD_REQUEST);
            }

            em.merge(customCustomer);
            return ResponseService.generateSuccessResponse("User details updated successfully", sharedUtilityService.breakReferenceForCustomer(customCustomer), HttpStatus.OK);

        }catch(NoSuchFieldException e)
        {
            return ResponseService.generateErrorResponse("No such field present :" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch(Exception e){
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error updating " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public boolean isFieldPresent (Class < ? > clazz, String fieldName){
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return field != null; // Field exists
        } catch (NoSuchFieldException e) {
            return false; // Field does not exist
        }
    }

    @Transactional
    @RequestMapping(value = "/get-customer-details/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<?> getUserDetails(@PathVariable Long customerId) {
        try {
            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
            if (customCustomer == null) {
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            }
            CustomerImpl customer = em.find(CustomerImpl.class, customerId);  // Assuming you retrieve the base Customer entity
            Map<String, Object> customerDetails = sharedUtilityService.breakReferenceForCustomer(customer);
            // Fetch qualification details and replace qualification_id with qualification_name

            List<Map<String, Object>> qualificationsWithNames = customCustomer.getQualificationDetailsList().stream()
                    .map(qualificationDetail -> {
                        // Create a new map to store qualification information
                        Map<String, Object> qualificationInfo = new HashMap<>();
                        // Fetch the qualification by qualification_id

                        //Qualification qualification = em.find(Qualification.class, qualificationDetail.getQualification_id());
                        Object id = qualificationDetail.getQualification_id();
                        Long qualificationId = id instanceof Long ? (Long) id : Long.valueOf((Integer) id);
                        Qualification qualification = em.find(Qualification.class, qualificationId);


                        // Populate the map with necessary fields from qualificationDetail
                        qualificationInfo.put("institution_name", qualificationDetail.getInstitution_name());
                        qualificationInfo.put("year_of_passing", qualificationDetail.getYear_of_passing());
                        qualificationInfo.put("board_or_university", qualificationDetail.getBoard_or_university());
                        qualificationInfo.put("subject_stream", qualificationDetail.getStream());
                        qualificationInfo.put("grade_or_percentage_value", qualificationDetail.getGrade_or_percentage_value());
                        qualificationInfo.put("marks_total", qualificationDetail.getTotal_marks());
                        qualificationInfo.put("marks_obtained", qualificationDetail.getMarks_obtained());

                        // Replace the qualification_id with qualification_name
                        if (qualification != null) {
                            qualificationInfo.put("qualification_name", qualification.getQualification_name());
                        } else {
                            qualificationInfo.put("qualification_name", "Unknown Qualification");
                        }

                        return qualificationInfo;
                    }).collect(Collectors.toList());


            customerDetails.put("qualificationDetails", qualificationsWithNames);

            List<Document> filteredDocuments = new ArrayList<>();

            for (Document document : customCustomer.getDocuments()) {
                if (document.getFilePath() != null && document.getDocumentType() != null) {
                    filteredDocuments.add(document);
                }
            }

            if (!filteredDocuments.isEmpty()) {
                customerDetails.put("documents", filteredDocuments);
            }


            return responseService.generateSuccessResponse("User details retrieved successfully", customerDetails, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving user details", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    @PostMapping("/upload-documents")
    public ResponseEntity<?> uploadDocuments(
            @RequestParam Long customerId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("fileTypes") List<Integer> fileTypes,
            @RequestParam(value = "removeFileTypes", required = false) Boolean removeFileTypes,
            @RequestHeader(value = "Authorization") String authHeader) {
        try {

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseService.generateErrorResponse("Authorization header is missing or invalid.", HttpStatus.UNAUTHORIZED);
            }

            if (customerId == null || files == null || fileTypes == null) {
                return ResponseService.generateErrorResponse("Invalid request parameters.", HttpStatus.BAD_REQUEST);
            }

            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            Long tokenUserId = jwtTokenUtil.extractId(jwtToken);

            String role = roleService.getRoleByRoleId(roleId).getRole_name();
            if (role == null) {
                return ResponseService.generateErrorResponse("Role not found for this user.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (!customerId.equals(tokenUserId)) {
                return ResponseService.generateErrorResponse("Unauthorized request.", HttpStatus.UNAUTHORIZED);
            }

            Map<Integer, List<MultipartFile>> groupedFiles = new HashMap<>();

            for (int i = 0; i < files.size(); i++) {
                Integer fileTypeId = fileTypes.get(i);
                MultipartFile file = files.get(i);

                groupedFiles.computeIfAbsent(fileTypeId, k -> new ArrayList<>()).add(file);
            }
            if (roleService.findRoleName(roleId).equals(Constant.roleUser)) {

                CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
                if (customCustomer == null) {
                    return ResponseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);
                }

                Map<String, Object> responseData = new HashMap<>();
                List<String> deletedDocumentMessages = new ArrayList<>();


                for (Map.Entry<Integer, List<MultipartFile>> entry : groupedFiles.entrySet()) {
                    Integer fileNameId = entry.getKey();
                    List<MultipartFile> fileList = entry.getValue();
                    for (MultipartFile file : fileList) {

                        DocumentType documentTypeObj = em.createQuery(
                                        "SELECT dt FROM DocumentType dt WHERE dt.document_type_id = :documentTypeId", DocumentType.class)
                                .setParameter("documentTypeId", fileNameId)
                                .getResultStream()
                                .findFirst()
                                .orElse(null);

                        if (documentTypeObj == null) {
                            return ResponseService.generateErrorResponse("Unknown document type for file: " + fileNameId, HttpStatus.BAD_REQUEST);
                        }

                        Document existingDocument = em.createQuery(
                                        "SELECT d FROM Document d WHERE d.custom_customer = :customCustomer " +
                                                "AND d.documentType = :documentType AND d.name IS NOT NULL ", Document.class)
                                .setParameter("customCustomer", customCustomer)
                                .setParameter("documentType", documentTypeObj)
                                .getResultStream()
                                .findFirst()
                                .orElse(null);


                        if (!DocumentStorageService.isValidFileType(file) && existingDocument == null) {
                            return ResponseEntity.badRequest().body(Map.of(
                                    "status", ApiConstants.STATUS_ERROR,
                                    "status_code", HttpStatus.BAD_REQUEST.value(),
                                    "message", "Invalid file type: " + file.getOriginalFilename()
                            ));
                        }

                        fileUploadService.uploadFileOnFileServer(file, documentTypeObj.getDocument_type_name(), customerId, role);


                        if (removeFileTypes != null && removeFileTypes) {

                            if (existingDocument != null && fileNameId != 13) {
                                if (existingDocument != null) {
                                    String filePath = existingDocument.getFilePath();

                                    if (filePath != null) {
                                        fileUploadService.deleteFile( customerId,  documentTypeObj.getDocument_type_name(),  existingDocument.getName(),  role);
                                    }

                                    existingDocument.setDocumentType(null);
                                    existingDocument.setFilePath(null);
                                    existingDocument.setName(null);
                                    em.persist(existingDocument);

                                    deletedDocumentMessages.add( documentTypeObj.getDocument_type_name() + "' has been deleted.");
                                }
                                continue;
                            }
                        }


                        if (fileNameId == 13 && (!file.isEmpty() || file != null)) {
                            String newFileName = file.getOriginalFilename();
                            // Check for existing document with the same name
                            Document existingDocument13 = em.createQuery(
                                            "SELECT d FROM Document d WHERE d.custom_customer = :customCustomer AND d.documentType = :documentType AND d.name = :documentName AND (d.name IS NOT NULL)", Document.class)
                                    .setParameter("customCustomer", customCustomer)
                                    .setParameter("documentType", documentTypeObj)
                                    .setParameter("documentName", newFileName)
                                    .getResultStream()
                                    .findFirst()
                                    .orElse(null);

                            if (existingDocument13 == null) {
                                documentStorageService.createDocument(file, documentTypeObj, customCustomer, customerId, role);
                            } else if (existingDocument13 != null) {
                                String filePath = existingDocument13.getFilePath();
                                if (removeFileTypes != null && removeFileTypes && newFileName!=null ) {
                                    fileUploadService.deleteFile( customerId,  documentTypeObj.getDocument_type_name(),  existingDocument.getName(),  role);
                                }
                                existingDocument13.setFilePath(null);
                                existingDocument13.setName(null);
                                existingDocument13.setCustom_customer(null);
                                em.merge(existingDocument);
                                deletedDocumentMessages.add( documentTypeObj.getDocument_type_name() + "' has been deleted.");
                            }
                        }
                        // If the file is not empty and a document already exists, update the document
                        else if (existingDocument != null && (!file.isEmpty() || file != null) && fileNameId != 13) {
                            String filePath = existingDocument.getFilePath();

                            if (filePath != null) {
                                String absolutePath = System.getProperty("user.dir") + "/../test/" + filePath;
                                File oldFile = new File(absolutePath);
                                String oldFileName = oldFile.getName();
                                String newFileName = file.getOriginalFilename();

                                if (!newFileName.equals(oldFileName)) {
                                    fileUploadService.deleteFile( customerId,  documentTypeObj.getDocument_type_name(),  existingDocument.getName(),  role);
                                    documentStorageService.updateOrCreateDocument(existingDocument, file, documentTypeObj, customerId, role);
                                }
                            }
                        } else {
                            // If the file is not empty create the document
                            if (!file.isEmpty() || file != null && (fileNameId != 13)) {
                                documentStorageService.createDocument(file, documentTypeObj, customCustomer, customerId, role);
                            }
                        }
                    }

                }

                if (!deletedDocumentMessages.isEmpty()) {
                    responseData.put("deletedMessages", deletedDocumentMessages);
                }

                return ResponseService.generateSuccessResponse("Documents updated successfully", responseData, HttpStatus.OK);

            } else {
                ServiceProviderEntity serviceProviderEntity = em.find(ServiceProviderEntity.class, customerId);
                if (serviceProviderEntity == null) {
                    return ResponseService.generateErrorResponse("No data found for this serviceProvider", HttpStatus.NOT_FOUND);
                }

                Map<String, Object> responseData = new HashMap<>();
                List<String> deletedDocumentMessages = new ArrayList<>();

                // Handle file uploads and deletions

                for (Map.Entry<Integer, List<MultipartFile>> entry : groupedFiles.entrySet()) {
                    Integer fileNameId = entry.getKey();
                    List<MultipartFile> fileList = entry.getValue();
                    for (MultipartFile file : fileList) {


                        DocumentType documentTypeObj = em.createQuery(
                                        "SELECT dt FROM DocumentType dt WHERE dt.document_type_id = :documentTypeId", DocumentType.class)
                                .setParameter("documentTypeId", fileNameId)
                                .getResultStream()
                                .findFirst()
                                .orElse(null);

                        if (documentTypeObj == null) {
                            return ResponseService.generateErrorResponse("Unknown document type for file: " + fileNameId, HttpStatus.BAD_REQUEST);
                        }

                        ServiceProviderDocument existingDocument = em.createQuery(
                                        "SELECT d FROM ServiceProviderDocument d WHERE d.serviceProviderEntity = :serviceProviderEntity AND d.documentType = :documentType AND d.name IS NOT NULL", ServiceProviderDocument.class)
                                .setParameter("serviceProviderEntity", serviceProviderEntity)
                                .setParameter("documentType", documentTypeObj)

                                .getResultStream()
                                .findFirst()
                                .orElse(null);

                        if (!DocumentStorageService.isValidFileType(file) && existingDocument == null) {
                            return ResponseEntity.badRequest().body(Map.of(
                                    "status", ApiConstants.STATUS_ERROR,
                                    "status_code", HttpStatus.BAD_REQUEST.value(),
                                    "message", "Invalid file type: " + file.getOriginalFilename()
                            ));
                        }
//                        documentStorageService.saveDocuments(file, documentTypeObj.getDocument_type_name(), customerId, role);

                        fileUploadService.uploadFileOnFileServer(file, documentTypeObj.getDocument_type_name(), customerId, role);

                        if (removeFileTypes != null && removeFileTypes) {
                            if (existingDocument != null && fileNameId != 13) {
                                if (existingDocument != null) {

                                    String filePath = existingDocument.getFilePath();
                                    if (filePath != null) {
                                        fileUploadService.deleteFile( customerId,  documentTypeObj.getDocument_type_name(),  existingDocument.getName(),  role);
                                    }
                                    existingDocument.setDocumentType(null);
                                    existingDocument.setName(null);
                                    existingDocument.setFilePath(null);
                                    existingDocument.setServiceProviderEntity(null);
                                    em.persist(existingDocument);

                                    deletedDocumentMessages.add(documentTypeObj.getDocument_type_name() + " has been deleted.");
                                }
                                continue;
                            }
                        }

                        if (fileNameId == 13 && (!file.isEmpty() || file != null)) {
                            String newFileName = file.getOriginalFilename();

                            // Check for existing document with the same name
                            ServiceProviderDocument existingDocument13 = em.createQuery(
                                            "SELECT d FROM ServiceProviderDocument d WHERE d.serviceProviderEntity = :serviceProviderEntity AND d.documentType = :documentType AND d.name = :documentName AND (d.name IS NOT NULL)", ServiceProviderDocument.class)
                                    .setParameter("serviceProviderEntity", serviceProviderEntity)
                                    .setParameter("documentType", documentTypeObj)
                                    .setParameter("documentName", newFileName)
                                    .getResultStream()
                                    .findFirst()
                                    .orElse(null);

                            if (existingDocument13 == null) {
                                documentStorageService.createDocumentServiceProvider(file, documentTypeObj, serviceProviderEntity, customerId, role);
                            }

                            else if (existingDocument13 != null) {
                                if (removeFileTypes != null && removeFileTypes && newFileName!=null ) {
                                    fileUploadService.deleteFile( customerId,  documentTypeObj.getDocument_type_name(),  existingDocument.getName(),  role);

                                }
                                existingDocument13.setFilePath(null);
                                existingDocument13.setName(null);
                                existingDocument13.setServiceProviderEntity(null);

                                em.merge(existingDocument13);
                                deletedDocumentMessages.add( documentTypeObj.getDocument_type_name() + "' has been deleted.");
                            }


                        }
                        // If the file is not empty and a document already exists, update the document
                        else if (existingDocument != null && (!file.isEmpty() || file != null) && fileNameId != 13) {
                            String filePath = existingDocument.getFilePath();
                            if (filePath != null) {

                                String absolutePath = System.getProperty("user.dir") + "/../test/" + filePath;
                                File oldFile = new File(absolutePath);
                                String oldFileName = oldFile.getName();
                                String newFileName = file.getOriginalFilename();
                                if (!newFileName.equals(oldFileName)) {
//                                    oldFile.delete();
                                    fileUploadService.deleteFile( customerId,  documentTypeObj.getDocument_type_name(),  existingDocument.getName(),  role);

                                    documentStorageService.updateOrCreateServiceProvider(existingDocument, file, documentTypeObj, customerId, role);
                                }
                            }
                        } else {
                            // If the file is not empty create the document
                            if (!file.isEmpty() || file != null && (fileNameId != 13)) {
                                documentStorageService.createDocumentServiceProvider(file, documentTypeObj, serviceProviderEntity, customerId, role);
                            }
                        }
                    }

                }
                return ResponseService.generateSuccessResponse("Documents updated successfully", responseData, HttpStatus.OK);
            }


        } catch (DataIntegrityViolationException e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Document with the same name and file path already exists." + e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error updating documents: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @RequestMapping(value = "update-username", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerUsername(@RequestBody Map<String, Object> updates, @RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            String username = (String) updates.get("username");
            if(username!=null)
                username=username.trim();

            if (username.isEmpty()||username.contains(" ")) {
                return ResponseService.generateErrorResponse("Invalid username", HttpStatus.NOT_FOUND);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);

            }
            Customer existingCustomerByUsername = null;
            existingCustomerByUsername = customerService.readCustomerByUsername(username);

            if ((existingCustomerByUsername != null) && !existingCustomerByUsername.getId().equals(customerId)) {
                return ResponseService.generateErrorResponse("Username is not available", HttpStatus.BAD_REQUEST);

            } else {
                if(customer.getUsername()!=null && customer.getUsername().equals(username))
                    return ResponseService.generateErrorResponse("Old and new username cannot be same", HttpStatus.BAD_REQUEST);
                customer.setUsername(username);
                em.merge(customer);
                return ResponseService.generateSuccessResponse("User name  updated successfully : ", sharedUtilityService.breakReferenceForCustomer(customer), HttpStatus.OK);

            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Error updating username", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @RequestMapping(value = "create-or-update-password", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerPassword(@RequestBody Map<String, Object> details, @RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            String password = (String) details.get("password");
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);
            }
            if (password != null) {
                if (customer.getPassword() == null || customer.getPassword().isEmpty()) {
                    customer.setPassword(passwordEncoder.encode(password));
                    em.merge(customer);
                    return ResponseService.generateSuccessResponse("Password Created", sharedUtilityService.breakReferenceForCustomer(customer), HttpStatus.OK);
                }
                if (!passwordEncoder.matches(password, customer.getPassword())) {
            /*if (customerDTO.getPassword() != null && customerDTO.getOldPassword() != null) {
                if (passwordEncoder.matches(customerDTO.getOldPassword(), customer.getPassword())) {
                    if (!customerDTO.getPassword().equals(customerDTO.getOldPassword())) {*/
                    customer.setPassword(passwordEncoder.encode(password));
                    em.merge(customer);
                    return ResponseService.generateSuccessResponse("Password Updated", sharedUtilityService.breakReferenceForCustomer(customer), HttpStatus.OK);
                    /*} else
                        return new ResponseEntity<>("Old password and new password can not be same!", HttpStatus.BAD_REQUEST);
                } else
                    return new ResponseEntity<>("The old password you provided is incorrect. Please try again with the correct old password", HttpStatus.BAD_REQUEST);
            }*/
                }
                return ResponseService.generateErrorResponse("Old Password and new Password cannot be same", HttpStatus.BAD_REQUEST);
            } else {
                return ResponseService.generateErrorResponse("Empty Password", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Error updating password", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "delete", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteCustomer(@RequestParam String customerId) {
        try {
            Long id = Long.valueOf(customerId);
            if (customerService == null) {
                return ResponseService.generateErrorResponse(ApiConstants.CUSTOMER_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Customer customer = customerService.readCustomerById(id);
            if (customer != null) {
                customerService.deleteCustomer(customer);
                return ResponseService.generateSuccessResponse("Record Deleted Successfully", "", HttpStatus.OK);
            } else {
                return ResponseService.generateErrorResponse("No Records found for this ID " + id, HttpStatus.NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in deleting customer: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @Transactional
    @RequestMapping(value = "add-address", method = RequestMethod.POST)
    public ResponseEntity<?> addAddress(@RequestParam Long customerId, @RequestBody Map<String, Object> addressDetails) {
        try {
            Long id = Long.valueOf(customerId);
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Customer customer = customerService.readCustomerById(id);
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
                if(!addressDetails.containsKey("inFunctionCall"))
                    em.merge(customer);
                addressDetails.remove("inFunctionCall");
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
                    return ResponseService.generateErrorResponse("Error saving address", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                addressDTO.setPhoneNumber(customCustomer.getMobileNumber());
                return ResponseService.generateSuccessResponse("Address added successfully : ", addressDTO, HttpStatus.OK);


            } else {
                return ResponseService.generateErrorResponse("No Records found for this ID", HttpStatus.NOT_FOUND);

            }
        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error saving Address", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @RequestMapping(value = "retrieve-address", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveAddressList(@RequestParam Long customerId) {
        try {
            Long customerID = Long.valueOf(customerId);
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            Customer customer = customerService.readCustomerById(customerID);
            if (customer != null) {
                List<CustomerAddress> addressList = customer.getCustomerAddresses();
                List<AddressDTO> listOfAddresses = new ArrayList<>();
                for (CustomerAddress customerAddress : addressList) {
                    AddressDTO addressDTO = makeAddressDTO(customerAddress);
                    listOfAddresses.add(addressDTO);
                }
                return ResponseService.generateSuccessResponse("Addresses details : ", listOfAddresses, HttpStatus.OK);
            } else {
                return ResponseService.generateErrorResponse("No data found for this customerId", HttpStatus.INTERNAL_SERVER_ERROR);

            }


        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error in retreiving Address", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @RequestMapping(value = "address-details", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveAddressList(@RequestParam Long customerId, @RequestParam Long addressId) {
        try {
            Long customerID = Long.valueOf(customerId);
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            Customer customer = customerService.readCustomerById(customerID);
            CustomerAddress customerAddress = customerAddressService.readCustomerAddressById(addressId);
            if (customerAddress == null) {
                return ResponseService.generateErrorResponse("Address not found", HttpStatus.NOT_FOUND);
            } else {
                return ResponseService.generateSuccessResponse("Address details : ", makeAddressDTO(customerAddress), HttpStatus.OK);

            }
        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error saving Address", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    public AddressDTO makeAddressDTO(CustomerAddress customerAddress) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressId(customerAddress.getAddress().getId());
        addressDTO.setAddress(customerAddress.getAddress().getAddressLine1());
        addressDTO.setPinCode(customerAddress.getAddress().getPostalCode());
        addressDTO.setState(customerAddress.getAddress().getStateProvinceRegion());
        addressDTO.setCity(customerAddress.getAddress().getCity());
        addressDTO.setCustomerId(customerAddress.getCustomer().getId());
        addressDTO.setAddressName(customerAddress.getAddressName());
        CustomCustomer customCustomer = em.find(CustomCustomer.class, customerAddress.getCustomer().getId());
        addressDTO.setPhoneNumber(customCustomer.getMobileNumber());
        return addressDTO;
    }

    public ResponseEntity<?> createAuthResponse(String token, Customer customer) {
        OtpEndpoint.ApiResponse authResponse = new OtpEndpoint.ApiResponse(token, sharedUtilityService.breakReferenceForCustomer(customer), HttpStatus.OK.value(), HttpStatus.OK.name(), "User has been logged in");
        return ResponseService.generateSuccessResponse("Token details : ", authResponse, HttpStatus.OK);
    }

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
    @Transactional
    @PostMapping("/save-form/{customer_id}")
    public ResponseEntity<?>saveForm(@PathVariable long customer_id,@RequestParam long product_id)
    {
        try{
            Long id = Long.valueOf(customer_id);

            CustomCustomer customer=entityManager.find(CustomCustomer.class,id);
            if(customer==null)
            {
                return ResponseService.generateErrorResponse("Customer not found",HttpStatus.NOT_FOUND);
            }
            CustomProduct product=entityManager.find(CustomProduct.class,product_id);
            if(product==null)
            {
                return ResponseService.generateErrorResponse(Constant.PRODUCTNOTFOUND,HttpStatus.NOT_FOUND);
            }
            List<CustomProduct>savedForms=customer.getSavedForms();
            if(savedForms.contains(product))
                return ResponseService.generateErrorResponse("You can save a form only once",HttpStatus.UNPROCESSABLE_ENTITY);
            savedForms.add(product);
            customer.setSavedForms(savedForms);
            entityManager.merge(customer);
            Map<String,Object>responseBody=new HashMap<>();
            Map<String,Object>formBody=sharedUtilityService.createProductResponseMap(product,null);
            return ResponseService.generateSuccessResponse("Form Saved",formBody,HttpStatus.OK);
        }
        catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return ResponseService.generateErrorResponse("Error saving Form : "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @DeleteMapping("/unsave-form/{customer_id}")
    public ResponseEntity<?>unSaveForm(@PathVariable long customer_id,@RequestParam long product_id)
    {
        try{
            CustomCustomer customer=entityManager.find(CustomCustomer.class,customer_id);
            if(customer==null)
            {
                return ResponseService.generateErrorResponse("Customer not found",HttpStatus.NOT_FOUND);
            }
            CustomProduct product=entityManager.find(CustomProduct.class,product_id);
            if(product==null)
            {
                return ResponseService.generateErrorResponse(Constant.PRODUCTNOTFOUND,HttpStatus.NOT_FOUND);
            }
            List<CustomProduct>savedForms=customer.getSavedForms();
            if(savedForms.contains(product))
                savedForms.remove(product);
            else
                return ResponseService.generateErrorResponse("Form not present in saved Form list",HttpStatus.UNPROCESSABLE_ENTITY);
            customer.setSavedForms(savedForms);
            entityManager.merge(customer);
            Map<String,Object>responseBody=new HashMap<>();
            Map<String,Object>formBody=sharedUtilityService.createProductResponseMap(product,null);
            return ResponseService.generateSuccessResponse("Form Removed",formBody,HttpStatus.OK);
        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error removing Form : "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(value = "/forms/show-saved-forms")
    public ResponseEntity<?> getSavedForms(HttpServletRequest request, @RequestParam long customer_id) throws Exception {
        try {
            CustomCustomer customer = entityManager.find(CustomCustomer.class, customer_id);
            if (customer == null)
                ResponseService.generateErrorResponse("Customer with this id not found", HttpStatus.NOT_FOUND);
            if (customer.getSavedForms().isEmpty())
                ResponseService.generateErrorResponse("Saved form list is empty", HttpStatus.NOT_FOUND);
            List<Map<String, Object>> listOfSavedProducts = new ArrayList<>();
            for (Product product : customer.getSavedForms()) {
                listOfSavedProducts.add(sharedUtilityService.createProductResponseMap(product, null));
            }
            return ResponseService.generateSuccessResponse("Forms saved : ", listOfSavedProducts, HttpStatus.OK);
        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>("SOMEEXCEPTIONOCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/forms/show-filled-forms")
    public ResponseEntity<?> getFilledFormsByUserId(HttpServletRequest request, @RequestParam long customer_id) throws Exception {
        try {
            CustomCustomer customer = entityManager.find(CustomCustomer.class, customer_id);
            if (customer == null)
                ResponseService.generateErrorResponse("Customer with this id not found", HttpStatus.NOT_FOUND);
            if (customer.getSavedForms().isEmpty())
                ResponseService.generateErrorResponse("Saved form list is empty", HttpStatus.NOT_FOUND);
            List<Map<String, Object>> listOfSavedProducts = new ArrayList<>();
            for (Product product : customer.getSavedForms()) {
                listOfSavedProducts.add(sharedUtilityService.createProductResponseMap(product, null));
            }
            return ResponseService.generateSuccessResponse("Forms saved : ", listOfSavedProducts, HttpStatus.OK);
        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse("SOME EXCEPTION OCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @GetMapping(value = "/forms/show-recommended-forms")
    public ResponseEntity<?> getRecommendedFormsByUserId(HttpServletRequest request, @RequestParam long customer_id) throws Exception {
        try {
            CustomCustomer customer = entityManager.find(CustomCustomer.class, customer_id);
            if (customer == null)
                ResponseService.generateErrorResponse("Customer with this id not found", HttpStatus.NOT_FOUND);
            if (customer.getSavedForms().isEmpty())
                ResponseService.generateErrorResponse("Saved form list is empty", HttpStatus.NOT_FOUND);
            List<Map<String, Object>> listOfSavedProducts = new ArrayList<>();
            for (Product product : customer.getSavedForms()) {
                listOfSavedProducts.add(sharedUtilityService.createProductResponseMap(product, null));
            }
            return ResponseService.generateSuccessResponse("Forms saved : ", listOfSavedProducts, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse("SOME EXCEPTION OCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @GetMapping("/get-all-customers")
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        try {

            int startPosition = offset * limit;
            TypedQuery<CustomCustomer> query = entityManager.createQuery(Constant.GET_ALL_CUSTOMERS, CustomCustomer.class);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);
            List<Map> results = new ArrayList<>();
            for (CustomCustomer customer : query.getResultList()) {
                Customer customerToadd = customerService.readCustomerById(customer.getId());
                results.add(sharedUtilityService.breakReferenceForCustomer(customerToadd));
            }
            return ResponseService.generateSuccessResponse("List of customers : ", results, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in customers: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @PostMapping("/set-referrer/{customer_id}/{service_provider_id}")
    public ResponseEntity<?> setReferrerForCustomer(@PathVariable Long customer_id, @PathVariable Long service_provider_id) {
        try {
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer_id);
            if (customCustomer == null)
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, service_provider_id);
            if (serviceProvider == null)
                return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.NOT_FOUND);
            if (customCustomer.getReferrerServiceProvider() != null)
                return ResponseService.generateErrorResponse("Referrer already set", HttpStatus.NOT_FOUND);
            customCustomer.setReferrerServiceProvider(serviceProvider);
            entityManager.merge(customCustomer);
            return ResponseService.generateSuccessResponse("Referrer Set", sharedUtilityService.serviceProviderDetailsMap(serviceProvider), HttpStatus.OK);
        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error setting customer's referrer " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}