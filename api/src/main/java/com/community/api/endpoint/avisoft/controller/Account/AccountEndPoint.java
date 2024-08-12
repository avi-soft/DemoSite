package com.community.api.endpoint.avisoft.controller.Account;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.avisoft.controller.Customer.CustomerEndpoint;
import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.TwilioService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import java.io.UnsupportedEncodingException;

import static org.apache.commons.lang.StringUtils.isNumeric;

@RestController
@RequestMapping(value = "/account",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class AccountEndPoint {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private EntityManager em;
    @Autowired
    private TwilioService twilioService;
    @Autowired
    private CustomCustomerService customCustomerService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @PostMapping("/loginWithOtp")
    @ResponseBody
    public ResponseEntity<String> verifyAndLogin(@RequestBody CustomCustomer customer, HttpSession session) {
        try {
            if (customer.getCountryCode() == null || customer.getCountryCode().isEmpty()) {
                customer.setCountryCode(Constant.COUNTRY_CODE);
            }
            if (customer.getMobileNumber() != null) {
                if (customCustomerService.isValidMobileNumber(customer.getMobileNumber()) && isNumeric(customer.getMobileNumber())) {
                    return loginWithPhoneOtp(customer, session);
                } else {
                    return ResponseEntity.badRequest().body("Mobile number is not valid");
                }
            } else {
                return loginWithUsernameOtp(customer, session);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some issue in login: " + e.getMessage());
        }
    }


    @PostMapping("/loginWithPassword")
    @ResponseBody
    public ResponseEntity<?> loginWithPassword(@RequestBody CustomCustomer customer, HttpSession session,HttpServletRequest request) {
        try {
            if (customer.getMobileNumber() != null) {
                if (customer.getCountryCode() == null || customer.getCountryCode().isEmpty()) {
                    customer.setCountryCode(Constant.COUNTRY_CODE);
                }
                if (customCustomerService.isValidMobileNumber(customer.getMobileNumber()) && isNumeric(customer.getMobileNumber())) {
                    return loginWithCustomerPassword(customer, session, request);
                } else {
                    return ResponseEntity.badRequest().body("Mobile number is not valid");
                }
            } else {
                return loginWithUsername(customer, session,request);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some issue in login: " + e.getMessage());
        }
    }
    @RequestMapping(value = "phone-otp", method = RequestMethod.POST)
    private ResponseEntity<String> loginWithPhoneOtp(@RequestBody CustomCustomer customerDetails, HttpSession session) throws UnsupportedEncodingException, UnsupportedEncodingException {

        String countryCode = Constant.COUNTRY_CODE;
        if (customerDetails.getCountryCode() == null || customerDetails.getCountryCode().isEmpty()) {
            countryCode = Constant.COUNTRY_CODE;
        }else{

            countryCode = customerDetails.getCountryCode();
        }
        String updated_mobile = customerDetails.getMobileNumber();
        if (customerDetails.getMobileNumber().startsWith("0")) {
            updated_mobile = customerDetails.getMobileNumber().substring(1);
        }else{
            updated_mobile = customerDetails.getMobileNumber();
        }
        CustomCustomer customerRecords = customCustomerService.findCustomCustomerByPhone(customerDetails.getMobileNumber(),customerDetails.getCountryCode());
        if (customerRecords == null) {
            return new ResponseEntity<>("No Records found", HttpStatus.NOT_FOUND);
        }


        if (customerService == null) {
            return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Customer customer = customerService.readCustomerById(customerRecords.getId());
        if (customer != null) {
            twilioService.sendOtpToMobile(updated_mobile,countryCode);

            String storedOtp = customerRecords.getOtp();
            return new ResponseEntity<>("OTP Sent on " + customerDetails.getMobileNumber() + " storedOtp is " + storedOtp, HttpStatus.OK);
        } else {
            return ResponseEntity.badRequest().body("Mobile number not found");

        }
    }

    @Transactional
    @RequestMapping(value = "login-with-username", method = RequestMethod.POST)
    public ResponseEntity<?> loginWithUsername(@RequestBody CustomCustomer customerDetails, HttpSession session ,HttpServletRequest request) {
        try {
            if (customerService == null) {
                return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerByUsername(customerDetails.getUsername());
            if (customer == null) {
                return new ResponseEntity<>("No records found for the provided username.", HttpStatus.NOT_FOUND);
            }
            if (passwordEncoder.matches(customerDetails.getPassword(), customer.getPassword())) {

                String tokenKey = "authToken_" + customerDetails.getMobileNumber();
                String existingToken = (String) session.getAttribute(tokenKey);
                String ipAddress = request.getRemoteAddr();
                String userAgent = request.getHeader("User-Agent");
                if (existingToken != null && jwtUtil.validateToken(existingToken,  ipAddress, userAgent)) {

                    return ResponseEntity.ok(CustomerEndpoint.createAuthResponse(existingToken, customer));
                } else {

                    String token = jwtUtil.generateToken(customer.getId(), "USER",ipAddress,userAgent);
                    session.setAttribute(tokenKey, token);
                    return ResponseEntity.ok(CustomerEndpoint.createAuthResponse(existingToken,customer));

                }
            } else {
                return ResponseEntity.badRequest().body("Invalid password");
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some issue in login: " + e.getMessage());
        }
    }
    @RequestMapping(value = "username-otp", method = RequestMethod.POST)
    private ResponseEntity<String> loginWithUsernameOtp(
            @RequestBody CustomCustomer customerDetails,HttpSession session) {
        try {
            if (customerService == null) {
                return new ResponseEntity<>("customerService is null ", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerByUsername(customerDetails.getUsername());
            if (customer == null) {
                return new ResponseEntity<>("No records found for the provided username.", HttpStatus.NOT_FOUND);
            }
            CustomCustomer customCustomer = em.find(CustomCustomer.class, customer.getId());
            if (customCustomer != null) {
                return twilioService.sendOtpToMobile(customCustomer.getMobileNumber(), Constant.COUNTRY_CODE);
            } else {
                return new ResponseEntity<>("No records found", HttpStatus.NO_CONTENT);
            }
        }catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Some Error in login", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping(value = "login-with-password", method = RequestMethod.POST)
    public ResponseEntity<?> loginWithCustomerPassword(@RequestBody CustomCustomer customerDetails, HttpSession session,
                                                       HttpServletRequest request) {
        try {
            if (customerDetails.getCountryCode() == null) {
                customerDetails.setCountryCode(Constant.COUNTRY_CODE);
            }
            if (customerService == null) {
                return new ResponseEntity<>("customerService is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (customerDetails.getMobileNumber() == null || customerDetails.getPassword() == null) {
                return new ResponseEntity<>("Invalid mobile number or password", HttpStatus.BAD_REQUEST);
            }

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(customerDetails.getMobileNumber(), customerDetails.getCountryCode());
            if (existingCustomer != null) {
                Customer customer = customerService.readCustomerById(existingCustomer.getId());
                if (passwordEncoder.matches(customerDetails.getPassword(), existingCustomer.getPassword())) {
                    String tokenKey = "authToken_" + customerDetails.getMobileNumber();
                    String existingToken = (String) session.getAttribute(tokenKey);
                    String ipAddress = request.getRemoteAddr();
                    String userAgent = request.getHeader("User-Agent");
                    if (existingToken != null && jwtUtil.validateToken(existingToken,  ipAddress, userAgent)) {

                        return ResponseEntity.ok(CustomerEndpoint.createAuthResponse(existingToken, customer));
                    } else {

                       String token = jwtUtil.generateToken(existingCustomer.getId(), "USER",ipAddress,userAgent);
                        session.setAttribute(tokenKey, token);
                        return ResponseEntity.ok(CustomerEndpoint.createAuthResponse(existingToken,customer));

                    }

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
