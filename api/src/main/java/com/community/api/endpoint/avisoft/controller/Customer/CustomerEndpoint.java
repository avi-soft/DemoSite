package com.community.api.endpoint.avisoft.controller.Customer;


import com.community.api.component.JwtUtil;
import com.community.api.dto.CategoryDto;
import com.community.api.dto.CustomCategoryWrapper;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.endpoint.avisoft.controller.otpmodule.OtpEndpoint;
import com.community.api.endpoint.customer.AddressDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.endpoint.customer.CustomerDTO;
import com.community.api.entity.CustomProduct;
import com.community.api.services.CategoryService;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.TwilioService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.exception.ExceptionHandlingService;
import org.apache.commons.math3.analysis.function.Add;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
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
    private CustomerService customerService;
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
    private CategoryService categoryService;

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
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @RequestMapping(value = "getCustomer", method = RequestMethod.GET)
    public ResponseEntity<Object> retrieveCustomerById(@RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return new ResponseEntity<>("Customer with this ID does not exist", HttpStatus.NOT_FOUND);
            } else {
    
                return new ResponseEntity<>(customer, HttpStatus.OK);
            }
        } catch (Exception e) {

            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error retrieving Customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomer(@RequestBody CustomCustomer customerDetails, @RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
            if (customCustomer == null) {
                return new ResponseEntity<>("No data found for this customerId", HttpStatus.NOT_FOUND);
            }
            if (customerDetails.getMobileNumber() != null) {
                if (customCustomerService.isValidMobileNumber(customerDetails.getMobileNumber()) == false)
                    return new ResponseEntity<>("Cannot update phoneNumber", HttpStatus.INTERNAL_SERVER_ERROR);
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
                    return new ResponseEntity<>("Username is not available", HttpStatus.BAD_REQUEST);
                }
                if (existingCustomerByEmail != null && !existingCustomerByEmail.getId().equals(customerId)) {
                    return new ResponseEntity<>("Email not available", HttpStatus.BAD_REQUEST);
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
            return new ResponseEntity<>(customer, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error updating", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "update-username", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerUsername(@RequestBody Map<String, Object> updates, @RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            String username = (String) updates.get("username");
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return new ResponseEntity<>("No data found for this customerId", HttpStatus.NOT_FOUND);
            }
            Customer existingCustomerByUsername = null;
            if (username != null) {
                existingCustomerByUsername = customerService.readCustomerByUsername(username);
            } else
                new ResponseEntity<>("username Empty", HttpStatus.BAD_REQUEST);

            if ((existingCustomerByUsername != null) && !existingCustomerByUsername.getId().equals(customerId)) {
                return new ResponseEntity<>("Username is not available", HttpStatus.BAD_REQUEST);

            } else {
                customer.setUsername(username);
                em.merge(customer);
                return new ResponseEntity<>(customer, HttpStatus.OK);
            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return new ResponseEntity<>("Error updating username", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "update-password", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerPassword(@RequestBody CustomerDTO customerDTO, @RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return new ResponseEntity<>("No data found for this customerId", HttpStatus.NOT_FOUND);
            }
            if (customer.getPassword() == null || customer.getPassword().isEmpty()) {
                customer.setPassword(passwordEncoder.encode(customerDTO.getPassword()));
                em.merge(customer);
                return new ResponseEntity<>(customer, HttpStatus.OK);
            }
            String password = customerDTO.getPassword();
            if (customerDTO.getPassword() != null && customerDTO.getOldPassword() != null) {
                if (passwordEncoder.matches(customerDTO.getOldPassword(), customer.getPassword())) {
                    if (!customerDTO.getPassword().equals(customerDTO.getOldPassword())) {
                        customer.setPassword(passwordEncoder.encode(password));
                        em.merge(customer);
                        return new ResponseEntity<>(customer, HttpStatus.OK);
                    } else
                        return new ResponseEntity<>("Old password and new password can not be same!", HttpStatus.BAD_REQUEST);
                } else
                    return new ResponseEntity<>("The old password you provided is incorrect. Please try again with the correct old password", HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>("Empty Password", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return new ResponseEntity<>("Error updating password", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "delete", method = RequestMethod.DELETE)
    public ResponseEntity<String> updateCustomer(@RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer != null) {
                customerService.deleteCustomer(customerService.readCustomerById(customerId));
                return new ResponseEntity<>("Record Deleted Successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("No Records found for this ID", HttpStatus.INTERNAL_SERVER_ERROR);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error deleting", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "add-address", method = RequestMethod.POST)
    public ResponseEntity<?> addAddress(@RequestParam Long customerId, @RequestBody Map<String, Object> addressDetails) {
        try {
            if (customerService == null) {
                return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
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
                    return new ResponseEntity<>("Error saving address", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                addressDTO.setPhoneNumber(customCustomer.getMobileNumber());
                return new ResponseEntity<>(addressDTO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("No Records found for this ID", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error saving Address", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "retrieve-address", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveAddressList(@RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            List<CustomerAddress>addressList=customer.getCustomerAddresses();
            List<AddressDTO>listOfAddresses=new ArrayList<>();
            for(CustomerAddress customerAddress:addressList)
            {
                AddressDTO addressDTO=makeAddressDTO(customerAddress);
                listOfAddresses.add(addressDTO);
            }
            return  new ResponseEntity<>(listOfAddresses,HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error saving Address", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @RequestMapping(value = "address-details", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveAddressList(@RequestParam Long customerId,@RequestParam Long addressId) {
        try {
            if (customerService == null) {
                return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            CustomerAddress customerAddress=customerAddressService.readCustomerAddressById(addressId);
            if(customerAddress==null)
            {
                return new ResponseEntity<>("Address not found",HttpStatus.NOT_FOUND);
            }
            else
               return new ResponseEntity<>(makeAddressDTO(customerAddress),HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error saving Address", HttpStatus.INTERNAL_SERVER_ERROR);
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
    public static ResponseEntity<OtpEndpoint.AuthResponse> createAuthResponse(String token, Customer customer ) {
        OtpEndpoint.AuthResponse authResponse = new OtpEndpoint.AuthResponse(token, customer);
        return ResponseEntity.ok(authResponse);
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

    @GetMapping(value = "/getAllCategories")
    public ResponseEntity<?> getCategories(HttpServletRequest request, @RequestParam(value = "limit", defaultValue = "20") int limit) {
        try {
            if (catalogService == null) {
                return new ResponseEntity<>("catalogService is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<Category> categories = this.catalogService.findAllCategories();
            List<CustomCategoryWrapper> activeCategories = new ArrayList<>();

            Iterator<Category> iterator = categories.iterator();
            while (iterator.hasNext()) {
                Category category = iterator.next();
                if ((((Status) category).getArchived() != 'Y' && category.getActiveEndDate() == null) || (((Status) category).getArchived() != 'Y' && category.getActiveEndDate().after(new Date()))) {

                    CustomCategoryWrapper wrapper = new CustomCategoryWrapper();
                    wrapper.wrapDetails(category, request);
                    activeCategories.add(wrapper);
                }
            }

            return new ResponseEntity<>(activeCategories, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>("SOMEEXCEPTIONOCCURRED" + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getProductsByUserId")
    public ResponseEntity<?> getProductsByCategoryId(HttpServletRequest request,@RequestParam(value = "id") String id) throws Exception{
        try {
            if (catalogService == null) {
                return new ResponseEntity<>("catalogService is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Long categoryId = Long.parseLong(id);
            if(categoryId <= 0){
                return new ResponseEntity<>("CATEGORYCANNOTBELESSTHANOREQAULZERO", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Category category = this.catalogService.findCategoryById(categoryId);

            if (category == null) {
                return new ResponseEntity<>("Category not Found", HttpStatus.INTERNAL_SERVER_ERROR);
            } else if (((Status) category).getArchived() == 'Y') {
                return new ResponseEntity<>("Category is Archived", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            List<BigInteger> productIdList = categoryService.getAllProductsByCategoryId(categoryId);
            List<CustomProductWrapper> products = new ArrayList<>();

            for (BigInteger productId : productIdList) {
                CustomProduct customProduct = entityManager.find(CustomProduct.class, productId.longValue());

                if(customProduct != null && (((Status) customProduct).getArchived() != 'Y' && customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {
                    CustomProductWrapper wrapper = new CustomProductWrapper();
                    wrapper.wrapDetails(customProduct);
                    products.add(wrapper);
                }
            }

            CategoryDto categoryDao = new CategoryDto();
            categoryDao.setCategoryId(category.getId());
            categoryDao.setCategoryName(category.getName());
            categoryDao.setProducts(products);
            categoryDao.setTotalProducts(Long.valueOf(products.size()));

            return ResponseEntity.status(HttpStatus.OK).body(categoryDao);

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return new ResponseEntity<>("SOMEEXCEPTIONOCCURRED: " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}