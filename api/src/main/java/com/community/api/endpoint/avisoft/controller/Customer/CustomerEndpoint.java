package com.community.api.endpoint.avisoft.controller.Customer;


import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.avisoft.controller.otpmodule.OtpEndpoint;
import com.community.api.endpoint.customer.AddressDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomProduct;
import com.community.api.services.*;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.exception.ExceptionHandlingService;
import org.broadleafcommerce.core.catalog.domain.Product;
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
    private TwilioService twilioService;
    private CustomCustomerService customCustomerService;
    private AddressService addressService;
    private CustomerAddressService customerAddressService;
    private JwtUtil jwtUtil;
    private SanitizerService sanitizerService;


    @Autowired
    private ExceptionHandlingService exceptionHandlingService;

    @Autowired
    private  JwtUtil jwtTokenUtil;

    @Autowired
    private  RoleService roleService;


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
    public  void setSanitizerService(SanitizerService sanitizerService){
        this.sanitizerService=sanitizerService;
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
    public void setTwilioService(TwilioService twilioService) {
        this.twilioService = twilioService;
    }

    @Autowired
    public void setCustomCustomerService(CustomCustomerService customCustomerService) {
        this.customCustomerService = customCustomerService;
    }
    @Autowired
    private static SharedUtilityService sharedUtilityService;

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
                return ResponseService.generateErrorResponse("Customer Service Not Initialized",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer with this ID does not exist" , HttpStatus.NOT_FOUND);

            } else {
                return ResponseService.generateSuccessResponse("Customer with this ID is found "+customerId,sharedUtilityService.breakReferenceForCustomer(customer) , HttpStatus.OK);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving Customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomer(@RequestBody CustomCustomer customerDetails, @RequestParam Long customerId) {

        try {
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if(customerDetails.getUsername()!=null)
                return ResponseService.generateErrorResponse("Cannot update username",HttpStatus.UNPROCESSABLE_ENTITY);
            if(customerDetails.getPassword()!=null)
                return ResponseService.generateErrorResponse("Cannot update password",HttpStatus.UNPROCESSABLE_ENTITY);
            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
            if (customCustomer == null) {
                return ResponseService.generateErrorResponse("No data found for this customerId",HttpStatus.NOT_FOUND);

            }
            if (customerDetails.getMobileNumber() != null) {
                if (!customCustomerService.isValidMobileNumber(customerDetails.getMobileNumber())){
                    return ResponseService.generateErrorResponse("Cannot update phoneNumber", HttpStatus.INTERNAL_SERVER_ERROR);

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
                    return ResponseService.generateErrorResponse("Cannot update phoneNumber", HttpStatus.INTERNAL_SERVER_ERROR);

                }
                if (existingCustomerByEmail != null && !existingCustomerByEmail.getId().equals(customerId)) {
                    return ResponseService.generateErrorResponse("Email not available", HttpStatus.BAD_REQUEST);
                }
            }
            customerDetails.setId(customerId);
            customerDetails.setMobileNumber(customCustomer.getMobileNumber());
            customerDetails.setQualificationList(customCustomer.getQualificationList());
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
            return ResponseService.generateSuccessResponse("User details updated successfully : ",sharedUtilityService.breakReferenceForCustomer(customer), HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error updating", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @PostMapping("/upload-documents")
        public ResponseEntity<?> updateCustomer(
            @RequestParam Long customerId,
            @RequestPart(value = "Aadhaar Card", required = false) MultipartFile aadharCard,
            @RequestPart(value = "PAN Card", required = false) MultipartFile panCard,
            @RequestPart(value = "Passport Size Photo", required = false) MultipartFile photo) {
        try {
        if (customerService == null) {
            return ResponseService.generateErrorResponse("Customer service is not initialized.",HttpStatus.INTERNAL_SERVER_ERROR);
        }

        CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
        if (customCustomer == null) {
            return ResponseService.generateErrorResponse("No data found for this customerId",HttpStatus.NOT_FOUND);

        }
        Map<String, Object> responseData = new HashMap<>();
        Map<String, MultipartFile> files = new HashMap<>();
        if (aadharCard != null) {
            if (files.containsKey("Aadhar Card")) {
                return ResponseService.generateErrorResponse("Only one aadhar card image is allowed", HttpStatus.BAD_REQUEST);
            }
            files.put("Aadhar Card", aadharCard);
        }
        if (panCard != null) {
            if (files.containsKey("PAN Card")) {
                return ResponseService.generateErrorResponse("Only one pan card image is allowed", HttpStatus.BAD_REQUEST);
            }
            files.put("PAN Card", panCard);
        }
        if (photo != null) {
            if (files.containsKey("Photo")) {
                return ResponseService.generateErrorResponse("Only one photo is allowed", HttpStatus.BAD_REQUEST);
            }
            files.put("Photo", photo);
        }


        try{
            for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
                String documentType = entry.getKey();
                MultipartFile file = entry.getValue();

                ResponseEntity<Map<String, Object>> savedResponse = documentStorageService.saveDocuments(file, documentType, customerId, "customer");
                Map<String, Object> responseBody = savedResponse.getBody();

                if (savedResponse.getStatusCode() == HttpStatus.OK) {
                    responseData.put(documentType, responseBody.get("data"));
                } else {
                    return ResponseService.generateErrorResponse("Error uploading " + documentType, HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error updating in documents", HttpStatus.INTERNAL_SERVER_ERROR);

        }
            return ResponseService.generateSuccessResponse("Documents uploaded successfully", responseData, HttpStatus.OK);
    } catch (Exception e) {
        exceptionHandling.handleException(e);
        return ResponseService.generateErrorResponse("Error updating", HttpStatus.INTERNAL_SERVER_ERROR);

    }

    }

   /* @PostMapping("/upload-documents")
    public ResponseEntity<?> uploadBasicDocuments(@RequestBody Map<String, Object> request, @RequestPart("files") MultipartFile[] files) {
        try {

            Long customerId = (Long) request.get("customerId");
            Integer role = (Integer) request.get("role");

            if (role == null) {
                return ResponseService.generateErrorResponse(ApiConstants.ROLE_EMPTY, HttpStatus.BAD_REQUEST);
            }
            if (customerId == null) {
                return ResponseService.generateErrorResponse("Customer Id can not be empty", HttpStatus.BAD_REQUEST);
            }

            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
            if (customCustomer == null) {
                return ResponseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);
            }
            List<DocumentType> allDocumentTypes = documentStorageService.getAllDocumentTypes();

            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    String documentTypeName = documentStorageService.getDocumentTypeFromMultipartFile(file, allDocumentTypes);
                    System.out.println(documentTypeName + " documentTypeName");
                    DocumentType documentType = em.createQuery("SELECT dt FROM DocumentType dt WHERE dt.document_type_name = :typeName", DocumentType.class)
                            .setParameter("typeName", documentTypeName)
                            .getResultStream()
                            .findFirst()
                            .orElse(null);
                    if (documentType == null) {
                        return ResponseService.generateErrorResponse("Document type not found: " + documentTypeName, HttpStatus.BAD_REQUEST);
                    }
                    String Role = "";
                    if(role==5){
                         Role = "CUSTOMER";
                    }else if(role==4){
                         Role = "SERVICE_PROVIDER";
                    }else{
                        Role = "Admin";
                    }

                    String fileName = file.getOriginalFilename();
                    try (InputStream fileInputStream = file.getInputStream()) {
                        documentStorageService.saveDocument(customerId.toString(), documentTypeName, fileName, fileInputStream, Role);
                        Document doc = new Document();
                        doc.setName(fileName);
                        doc.setFilePath(DocumentStorageService.BASE_DIRECTORY + File.separator   + Role + File.separator + customerId + File.separator + documentTypeName + File.separator + fileName);
                        doc.setData(file.getBytes());
                        doc.setCustomCustomer(customCustomer);
                        doc.setDocumentType(documentType);
                        em.persist(doc);
                    } catch (Exception e) {
                        exceptionHandling.handleException(e);
                        return ResponseService.generateErrorResponse("Error processing file: " + fileName, HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            } else {
                return ResponseService.generateErrorResponse("No files provided for upload", HttpStatus.BAD_REQUEST);
            }

            return ResponseService.generateSuccessResponse("Documents uploaded successfully", null, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error updating user details", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
*/

    @Transactional
    @RequestMapping(value = "update-username", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerUsername(@RequestBody Map<String, Object> updates, @RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            String username = (String) updates.get("username");
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);

            }
            Customer existingCustomerByUsername = null;
            if (username != null) {
                existingCustomerByUsername = customerService.readCustomerByUsername(username);
            } else{
                return ResponseService.generateErrorResponse("username Empty", HttpStatus.BAD_REQUEST);

            }
            if(customer.getUsername()==null)
            {
                customer.setUsername(username);
                em.merge(customer);
                return ResponseService.generateSuccessResponse("Username set successfully for customer",sharedUtilityService.breakReferenceForCustomer(customer),HttpStatus.OK);
            }
            if(customer.getUsername().equals(username))
                return ResponseService.generateErrorResponse("New username and old username cannot be same", HttpStatus.BAD_REQUEST);

            if ((existingCustomerByUsername != null) && !existingCustomerByUsername.getId().equals(customerId)) {
                return ResponseService.generateErrorResponse("Username is not available", HttpStatus.BAD_REQUEST);

            } else {
                customer.setUsername(username);
                em.merge(customer);
                return ResponseService.generateSuccessResponse("User name  updated successfully : ",sharedUtilityService.breakReferenceForCustomer(customer), HttpStatus.OK);

            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Error updating username", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @RequestMapping(value = "create-or-update-password", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerPassword(@RequestBody Map<String,Object>details, @RequestParam Long customerId) {
        try {
            if(!sharedUtilityService.validateInputMap(details).equals(SharedUtilityService.ValidationResult.SUCCESS))
            {
                return ResponseService.generateErrorResponse("Invalid Request Body",HttpStatus.UNPROCESSABLE_ENTITY);
            }
            details=sanitizerService.sanitizeInputMap(details);
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.",HttpStatus.INTERNAL_SERVER_ERROR);

            }
            String password=(String) details.get("password");
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);
            }
            if(password!=null) {
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
                return ResponseService.generateErrorResponse("Old Password and new Password cannot be same",HttpStatus.BAD_REQUEST);
            }else {
                return ResponseService.generateErrorResponse("Empty Password", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return ResponseService.generateErrorResponse("Error updating password", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "delete", method = RequestMethod.DELETE)
    public ResponseEntity<?> updateCustomer(@RequestParam Long customer_id) {
        try {
            if (customerService == null) {
                return ResponseService.generateErrorResponse(ApiConstants.CUSTOMER_SERVICE_NOT_INITIALIZED, HttpStatus.INTERNAL_SERVER_ERROR);

            }
            Customer customer = customerService.readCustomerById(customer_id);
            if (customer != null) {
                customerService.deleteCustomer(customerService.readCustomerById(customer_id));
                return ResponseService.generateSuccessResponse("Record Deleted Successfully","", HttpStatus.OK);

            } else {
                return ResponseService.generateErrorResponse("No Records found for this ID " + customer_id, HttpStatus.NOT_FOUND);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in deleting customer " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "add-address", method = RequestMethod.POST)
    public ResponseEntity<?> addAddress(@RequestParam Long customerId, @RequestBody Map<String, Object> addressDetails) {
        try {
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
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
                    return ResponseService.generateErrorResponse("Error saving address", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                addressDTO.setPhoneNumber(customCustomer.getMobileNumber());
                return ResponseService.generateSuccessResponse("Address added successfully : ",addressDTO, HttpStatus.OK);


            } else {
                return ResponseService.generateErrorResponse("No Records found for this ID", HttpStatus.NOT_FOUND);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error saving Address", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @Transactional
    @RequestMapping(value = "retrieve-address", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveAddressList(@RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

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
                return ResponseService.generateSuccessResponse("Addresses details : ",listOfAddresses, HttpStatus.OK);
            }else{
                return ResponseService.generateErrorResponse("No data found for this customerId", HttpStatus.INTERNAL_SERVER_ERROR);

            }


        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error in retreiving Address", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
    @Transactional
    @RequestMapping(value = "address-details", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveAddressList(@RequestParam Long customerId,@RequestParam Long addressId) {
        try {
            if (customerService == null) {
                return ResponseService.generateErrorResponse("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);

            }
            Customer customer = customerService.readCustomerById(customerId);
            CustomerAddress customerAddress=customerAddressService.readCustomerAddressById(addressId);
            if(customerAddress==null)
            {
                return ResponseService.generateErrorResponse("Address not found",HttpStatus.NOT_FOUND);
            }
            else{
                return ResponseService.generateSuccessResponse("Address details : ", makeAddressDTO(customerAddress), HttpStatus.OK);

            }
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error saving Address", HttpStatus.INTERNAL_SERVER_ERROR);

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
    public static ResponseEntity<?> createAuthResponse(String token, Customer customer ) {
        OtpEndpoint.ApiResponse authResponse = new OtpEndpoint.ApiResponse(token, sharedUtilityService.breakReferenceForCustomer(customer), HttpStatus.OK.value(), HttpStatus.OK.name(),"User has been logged in");
        return ResponseService.generateSuccessResponse("Token details : ", authResponse, HttpStatus.OK);
    }



    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("Token is required");
        }
        try {
            jwtUtil.logoutUser(token);

            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error logging out",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @PostMapping("/save-form")
    public ResponseEntity<?>saveForm(@RequestParam long customer_id,@RequestParam long product_id)
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
                return ResponseService.generateErrorResponse("You can save a form only once",HttpStatus.UNPROCESSABLE_ENTITY);
            savedForms.add(product);
            customer.setSavedForms(savedForms);
            entityManager.merge(customer);
            Map<String,Object>responseBody=new HashMap<>();
            Map<String,Object>formBody=sharedUtilityService.createProductResponseMap(product,null);
            return ResponseService.generateSuccessResponse("Form Saved",formBody,HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error saving Form : "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @DeleteMapping("/unsave-form")
    public ResponseEntity<?>unSaveForm(@RequestParam long customer_id,@RequestParam long product_id)
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
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error removing Form : "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


   @GetMapping(value = "/forms/show-saved-forms")
    public ResponseEntity<?> getSavedForms(HttpServletRequest request,@RequestParam long  customer_id) throws Exception{
       try {
          CustomCustomer customer=entityManager.find(CustomCustomer.class,customer_id);
          if(customer==null)
              ResponseService.generateErrorResponse("Customer with this id not found",HttpStatus.NOT_FOUND);
          if(customer.getSavedForms().isEmpty())
              ResponseService.generateErrorResponse("Saved form list is empty",HttpStatus.NOT_FOUND);
          List<Map<String,Object>>listOfSavedProducts=new ArrayList<>();
          for(Product product:customer.getSavedForms())
          {
              listOfSavedProducts.add(sharedUtilityService.createProductResponseMap(product,null));
          }
          return ResponseService.generateSuccessResponse("Forms saved : ",listOfSavedProducts,HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>("SOMEEXCEPTIONOCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/forms/show-filled-forms")
    public ResponseEntity<?> getFilledFormsByUserId(HttpServletRequest request,@RequestParam long customer_id) throws Exception{
        try {
            CustomCustomer customer=entityManager.find(CustomCustomer.class,customer_id);
            if(customer==null)
                ResponseService.generateErrorResponse("Customer with this id not found",HttpStatus.NOT_FOUND);
            if(customer.getSavedForms().isEmpty())
                ResponseService.generateErrorResponse("Saved form list is empty",HttpStatus.NOT_FOUND);
            List<Map<String,Object>>listOfSavedProducts=new ArrayList<>();
            for(Product product:customer.getSavedForms())
            {
                listOfSavedProducts.add(sharedUtilityService.createProductResponseMap(product,null));
            }
            return ResponseService.generateSuccessResponse("Forms saved : ",listOfSavedProducts,HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>("SOMEEXCEPTIONOCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping(value = "/forms/show-recommended-forms")
    public ResponseEntity<?> getRecommendedFormsByUserId(HttpServletRequest request,@RequestParam long customer_id) throws Exception{
        try {
            CustomCustomer customer=entityManager.find(CustomCustomer.class,customer_id);
            if(customer==null)
                ResponseService.generateErrorResponse("Customer with this id not found",HttpStatus.NOT_FOUND);
            if(customer.getSavedForms().isEmpty())
                ResponseService.generateErrorResponse("Saved form list is empty",HttpStatus.NOT_FOUND);
            List<Map<String,Object>>listOfSavedProducts=new ArrayList<>();
            for(Product product:customer.getSavedForms())
            {
                listOfSavedProducts.add(sharedUtilityService.createProductResponseMap(product,null));
            }
            return ResponseService.generateSuccessResponse("Forms saved : ",listOfSavedProducts,HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>("SOMEEXCEPTIONOCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}