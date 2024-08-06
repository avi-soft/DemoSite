package com.community.api.endpoint.avisoft.controller.Customer;

import com.community.api.endpoint.avisoft.controller.otpmodule.OtpEndpoint;
import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.endpoint.customer.CustomerDTO;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.TwilioService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.apache.http.protocol.HTTP;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.security.Principal;

@RestController
@RequestMapping(value = "/customer",
        produces = {
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.APPLICATION_XML_VALUE
        }
)

public class CustomerEndpoint {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private EntityManager em;
    @Autowired
    private TwilioService twilioService;
    @Autowired
    private CustomCustomerService customCustomerService;

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
            customerDetails.setMobileNumber(customCustomer.getMobileNumber());
            customerDetails.setCountryCode(customCustomer.getCountryCode());
            Customer customer = customerService.readCustomerById(customerId);
            //using reflections
            for (Field field : CustomCustomer.class.getDeclaredFields()) {
                field.setAccessible(true);
                Object newValue = field.get(customerDetails);
                System.out.println(field);
                if (newValue != null) {
                    field.set(customCustomer, newValue);
                }
            }
            if (customerDetails.getFirstName() != null || customerDetails.getLastName() != null) {
                customer.setFirstName(customerDetails.getFirstName());
                customer.setLastName(customerDetails.getLastName());
            }
            em.merge(customCustomer);
            return new ResponseEntity<>(customer, HttpStatus.OK);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error updating", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "update-username", method = RequestMethod.POST)
    public ResponseEntity<?> updateCustomerUsername(@RequestBody CustomerDTO customerDTO, @RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer=customerService.readCustomerById(customerId);
            if (customer == null) {
                return new ResponseEntity<>("No data found for this customerId", HttpStatus.NOT_FOUND);
            }
            String username=customerDTO.getUsername();
            Customer existingCustomerByUsername = null;
            if ( username != null) {
                existingCustomerByUsername = customerService.readCustomerByUsername(username);
            }
            else
                new ResponseEntity<>("username Empty", HttpStatus.BAD_REQUEST);

            if ((existingCustomerByUsername != null)&&!existingCustomerByUsername.getId().equals(customerId)) {
                return new ResponseEntity<>("Username is not available", HttpStatus.BAD_REQUEST);
                }
                else
                {
                    customer.setUsername(username);
                    em.merge(customer);
                    return new ResponseEntity<>(customer,HttpStatus.OK);
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
            Customer customer=customerService.readCustomerById(customerId);
            if (customer == null) {
                return new ResponseEntity<>("No data found for this customerId", HttpStatus.NOT_FOUND);
            }
            if(customer.getPassword()==null||customer.getPassword().isEmpty())
            {
                customer.setPassword(passwordEncoder.encode(customerDTO.getPassword()));
                em.merge(customer);
                return new ResponseEntity<>(customer, HttpStatus.NOT_FOUND);
            }
            String password= customerDTO.getPassword();
            if (customerDTO.getPassword() != null&&customerDTO.getOldPassword()!=null) {
                if (passwordEncoder.matches(customerDTO.getOldPassword(),customer.getPassword())) {
                    if(!customerDTO.getPassword().equals(customerDTO.getOldPassword())) {
                        customer.setPassword(passwordEncoder.encode(password));
                        em.merge(customer);
                        return new ResponseEntity<>(customer, HttpStatus.OK);
                    }
                    else
                        return new ResponseEntity<>("Old password and new password can not be same!", HttpStatus.BAD_REQUEST);
                }
                else
                    return new ResponseEntity<>("The old password you provided is incorrect. Please try again with the correct old password", HttpStatus.BAD_REQUEST);
            }
            else
            {
                return new ResponseEntity<>("Empty Password",HttpStatus.BAD_REQUEST);
            }
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            return new ResponseEntity<>("Error updating password", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @RequestMapping(value = "delete", method = RequestMethod.DELETE)
    public ResponseEntity < String > updateCustomer(@RequestParam Long customerId) {
        try {
            if (customerService == null) {
                return new ResponseEntity < > ("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer != null) {
                customerService.deleteCustomer(customerService.readCustomerById(customerId));
                return new ResponseEntity<>("Record Deleted Successfully", HttpStatus.OK);
            }
            else
            {
                return new ResponseEntity<>("No Records found for this ID", HttpStatus.INTERNAL_SERVER_ERROR);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity < > ("Error deleting", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static ResponseEntity<OtpEndpoint.AuthResponse> createAuthResponse(String token, Customer customer ) {
        customer.setPassword(null);
        OtpEndpoint.AuthResponse authResponse = new OtpEndpoint.AuthResponse(token, customer);
        return ResponseEntity.ok(authResponse);
    }
}