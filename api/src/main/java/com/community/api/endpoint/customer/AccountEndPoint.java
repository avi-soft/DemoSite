package com.community.api.endpoint.customer;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.TwilioService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;


import static org.apache.commons.lang.StringUtils.isNumeric;

@RestController
@RequestMapping(value = "/account",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class AccountEndPoint {
    String phoneQuery = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber";
    final String COUNTRY_CODE="+91";
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
    @PostMapping("/loginWithOtp")
    @ResponseBody
    public ResponseEntity<String> verifyAndLogin(@RequestBody CustomCustomer customer,
            HttpSession session) throws UnsupportedEncodingException {

        if (customer.getCountryCode() == null || customer.getCountryCode().isEmpty()) {
            customer.setCountryCode(COUNTRY_CODE);
        }
        if(customer.getMobileNumber()!=null)
        {
        if (customCustomerService.isValidMobileNumber(customer.getMobileNumber()) && isNumeric(customer.getMobileNumber())) {
            if(customer.getCountryCode()==null||customer.getCountryCode().isEmpty())
                customer.setCountryCode(COUNTRY_CODE);
            return loginWithPhoneOtp(customer,session);
        } }
        else {
            return loginWithUsernameOtp(customer,session);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/loginWithPassword")
    @ResponseBody
    public ResponseEntity<String> loginWithPassword(@RequestBody CustomCustomer customer,
                                                 HttpSession session) throws UnsupportedEncodingException {

        if(customer.getMobileNumber()!=null)
        {
            if (customCustomerService.isValidMobileNumber(customer.getMobileNumber()) && isNumeric(customer.getMobileNumber())) {
                if(customer.getCountryCode()==null||customer.getCountryCode().isEmpty())
                    customer.setCountryCode(COUNTRY_CODE);
                return loginWithCustomerPassword(customer,session);
            } }
        else {
            return loginWithUsername(customer,session);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @RequestMapping(value = "phone-otp", method = RequestMethod.POST)
    private ResponseEntity<String> loginWithPhoneOtp(@RequestBody CustomCustomer customerDetails,HttpSession session) throws UnsupportedEncodingException {

        CustomCustomer customerRecords = customCustomerService.findCustomCustomerByPhone(customerDetails.getMobileNumber());
        if(customerRecords==null)
        {
            return new ResponseEntity<>("No records found", HttpStatus.NO_CONTENT);
        }
        if (customerService == null) {
            return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Customer customer = customerService.readCustomerById(customerRecords.getId());
        //above lines finds the customCustomer by phone and then using its id to find customer in blcCustomer
        if (customer != null) {
            //twilioService.sendOtpToMobile(customerDetails.getMobileNumber());
            return new ResponseEntity<>("OTP Sent"+customerRecords.getId(), HttpStatus.OK);
        } else {
            return ResponseEntity.badRequest().body("Mobile number not found");
        }
    }
    @Transactional
    @RequestMapping(value = "login-with-username", method = RequestMethod.POST)
    private ResponseEntity<String> loginWithUsername(
            @RequestBody CustomCustomer customerDetails,HttpSession session) {
        if (customerService == null) {
            System.out.println("Customer service is not initialized.");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Customer customer=customerService.readCustomerByUsername(customerDetails.getUsername());
        if(customer==null)
        {
            return new ResponseEntity<>("No records found", HttpStatus.NO_CONTENT);
        }
            if (customer.getPassword().equals(customerDetails.getPassword())) {
                return ResponseEntity.ok("Login successful");
            } else {
                return ResponseEntity.badRequest().body("Invalid password");
            }
        }
    @RequestMapping(value = "username-otp", method = RequestMethod.POST)
    private ResponseEntity<String> loginWithUsernameOtp(
            @RequestBody CustomCustomer customerDetails,HttpSession session) {
        if (customerService == null) {
            System.out.println("Customer service is not initialized.");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Customer customer=customerService.readCustomerByUsername(customerDetails.getUsername());
        if(customer==null)
        {
            System.out.println("Customer not found");
            return new ResponseEntity<>("No records found", HttpStatus.NO_CONTENT);
        }
        CustomCustomer customCustomer=em.find(CustomCustomer.class,customer.getId());
        if(customCustomer!=null) {
        return new ResponseEntity<>("Records found ,number = "+customCustomer.getMobileNumber(), HttpStatus.OK);
            //return twilioService.sendOtpToMobile(customerDetails.getMobileNumber());
        }
        else {
            System.out.println("Custom Customer not found");
            return new ResponseEntity<>("No records found", HttpStatus.NO_CONTENT);
        }
    }
    @RequestMapping(value = "login-with-password", method = RequestMethod.POST)
    public ResponseEntity<String> loginWithCustomerPassword(@RequestBody CustomCustomer customerDetails,HttpSession session) {
        try {
            if (customerService == null) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (customerDetails.getMobileNumber() == null || customerDetails.getPassword() == null) {
                return new ResponseEntity<>("Invalid mobile number or password", HttpStatus.BAD_REQUEST);
            }


            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(customerDetails.getMobileNumber());
            if (existingCustomer != null) {
                Customer customer=customerService.readCustomerById(existingCustomer.getId());
                if (customer.getPassword().equals(customerDetails.getPassword())) {
                    return new ResponseEntity<>("Log in Successfull", HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("Incorrect Password", HttpStatus.UNAUTHORIZED);
                }
            } else {
                return new ResponseEntity<>("Customer does not exist", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error retrieving Customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
