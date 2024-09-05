package com.community.api.endpoint.avisoft.controller.Customer;


import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;

import com.community.api.dto.AddCategoryDto;

import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.DocumentDTO;
import com.community.api.endpoint.avisoft.controller.otpmodule.OtpEndpoint;
import com.community.api.endpoint.customer.AddressDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomProduct;
import com.community.api.services.*;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.exception.ExceptionHandlingService;

import com.community.api.services.exception.FileSizeExceededException;
import com.community.api.services.exception.InvalidFileTypeException;
import com.community.api.utils.Document;

import com.community.api.utils.DocumentType;

import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Category;
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
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
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
    public void setTwilioService(TwilioService twilioService) {
        this.twilioService = twilioService;
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
    @RequestMapping(value = "update", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateCustomer(
            @RequestParam Long customerId,
            @RequestPart("customerDetails") CustomCustomer customerDetails,
            @RequestPart(value = "aadharCard", required = false) MultipartFile aadharCard,
            @RequestPart(value = "panCard", required = false) MultipartFile panCard,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestHeader(value = "Authorization") String authHeader) {
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
            String jwtToken = authHeader.substring(7);
            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();

            Map<String, MultipartFile> files = new HashMap<>();
            if (aadharCard != null) {
                if (files.containsKey("Aadhar Card")) {
                    return responseService.generateErrorResponse("Only one aadhar card image is allowed", HttpStatus.BAD_REQUEST);
                }
                files.put("Aadhar Card", aadharCard);
            }
            if (panCard != null) {
                if (files.containsKey("PAN Card")) {
                    return responseService.generateErrorResponse("Only one pan card image is allowed", HttpStatus.BAD_REQUEST);
                }
                files.put("PAN Card", panCard);
            }
            if (photo != null) {
                if (files.containsKey("Photo")) {
                    return responseService.generateErrorResponse("Only one photo is allowed", HttpStatus.BAD_REQUEST);
                }
                files.put("Photo", photo);
            }

            for (Map.Entry<String, MultipartFile> entry : files.entrySet()) {
                String documentType = entry.getKey();
                MultipartFile file = entry.getValue();
                try {
                    if (!DocumentStorageService.isValidFileType(file)) {
                        throw new InvalidFileTypeException("Invalid file type: " + file.getOriginalFilename());
                    }
                    if (file.getSize() > Constant.MAX_FILE_SIZE) {
                        throw new FileSizeExceededException("File size exceeds the maximum allowed size: " + file.getOriginalFilename());
                    }

                    String fileName = file.getOriginalFilename();
                    try (InputStream fileInputStream = file.getInputStream()) {
                        documentStorageService.saveDocument(customerId.toString(), documentType, fileName, fileInputStream, role);
                    }

                    Document doc = new Document();
                    DocumentType documentTypeobj = new DocumentType();
                    doc.setName(fileName);
                    doc.setFilePath(DocumentStorageService.BASE_DIRECTORY + "\\" + role + "\\" + customerId + "\\" + documentType + "\\" + fileName);
                    doc.setData(file.getBytes());
                    doc.setCustomCustomer(customCustomer);
                    doc.setDocumentType(documentTypeobj);
                    em.persist(doc);
                } catch (Exception e) {
                    return responseService.generateErrorResponse("Error uploading document", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            em.merge(customCustomer);
            return responseService.generateSuccessResponse("User details updated successfully : ",customer, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error updating", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @PostMapping("/upload-documents")
    public ResponseEntity<?> uploadDocuments(@RequestParam Long customerId, @RequestPart("files") MultipartFile[] files) {
        try {
            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
            if (customCustomer == null) {
                return responseService.generateErrorResponse("No data found for this customerId", HttpStatus.NOT_FOUND);
            }
            System.out.println(files + " files");
            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    String documentType = DocumentStorageService.getDocumentTypeFromMultipartFile(file);
                    String fileName = file.getOriginalFilename();
                    try (InputStream fileInputStream = file.getInputStream()) {
                        documentStorageService.saveDocument(customerId.toString(), documentType, fileName, fileInputStream, "customer");

                        Document doc = new Document();
                        DocumentType newDocumentType = new DocumentType();
                        doc.setName(fileName);
                        doc.setFilePath(DocumentStorageService.BASE_DIRECTORY + customerId + "\\" + documentType + "\\" + fileName);
                        doc.setData(file.getBytes());
                        doc.setCustomCustomer(customCustomer);
                        doc.setDocumentType(newDocumentType);
                        em.persist(doc);
                    } catch (Exception e) {
                        exceptionHandling.handleException(e);
                        return responseService.generateErrorResponse("Error uploading documents", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            }

            return responseService.generateSuccessResponse("Documents uploaded successfully", null, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error uploading documents", HttpStatus.INTERNAL_SERVER_ERROR);
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
    public static ResponseEntity<?> createAuthResponse(String token, Customer customer ) {
        OtpEndpoint.ApiResponse authResponse = new OtpEndpoint.ApiResponse(token, customer, HttpStatus.OK.value(), HttpStatus.OK.name(),"User has been logged in");
        return responseService.generateSuccessResponse("Token details : ", authResponse, HttpStatus.OK);
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