package com.community.api.endpoint.avisoft.controller.Customer;


import com.community.api.component.JwtUtil;
import com.community.api.endpoint.avisoft.controller.otpmodule.OtpEndpoint;
import com.community.api.endpoint.customer.AddressDTO;
import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.endpoint.customer.CustomerDTO;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.TwilioService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.apache.commons.math3.analysis.function.Add;
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
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/customer",
        produces = {
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_XML_VALUE
        }
)

public class CustomerEndpoint {
    private final PasswordEncoder passwordEncoder;
    private final CustomerService customerService;
    private final ExceptionHandlingImplement exceptionHandling;
    private final EntityManager em;
    private final TwilioService twilioService;
    private final CustomCustomerService customCustomerService;
    private final AddressService addressService;
    private final CustomerAddressService customerAddressService;

    public CustomerEndpoint(PasswordEncoder passwordEncoder,
                     CustomerService customerService,
                     ExceptionHandlingImplement exceptionHandling,
                     EntityManager em,
                     TwilioService twilioService,
                     CustomCustomerService customCustomerService,
                     AddressService addressService,
                     CustomerAddressService customerAddressService) {
        this.passwordEncoder = passwordEncoder;
        this.customerService = customerService;
        this.exceptionHandling = exceptionHandling;
        this.em = em;
        this.twilioService = twilioService;
        this.customCustomerService = customCustomerService;
        this.addressService = addressService;
        this.customerAddressService = customerAddressService;
    }
    @Autowired
    private JwtUtil jwtUtil;
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
                customer.setPassword(null);
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
                return new ResponseEntity<>(customer, HttpStatus.NOT_FOUND);
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
        customer.setPassword(null);
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

}