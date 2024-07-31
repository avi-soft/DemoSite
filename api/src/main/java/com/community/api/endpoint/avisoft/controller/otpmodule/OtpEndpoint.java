package com.community.api.endpoint.avisoft.controller.otpmodule;
import com.community.api.component.Constant;
import com.community.api.component.JwtAuthenticationFilter;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.TwilioService;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/otp")
public class OtpEndpoint {

    private static final Logger log = LoggerFactory.getLogger(OtpEndpoint.class);
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    private final TwilioService twilioService;
    private static final Logger logger = LoggerFactory.getLogger(OtpEndpoint.class);

    @Autowired
    private CustomCustomerService customCustomerService;
    @Autowired
    private JwtUtil jwtUtil;
    public OtpEndpoint( TwilioService twilioService) {

        this.twilioService = twilioService;
    }
    @Autowired
    private EntityManager em;
    @Autowired
    private CustomerService customerService;

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendtOtp(@RequestBody CustomCustomer customerDetails,HttpSession session) throws UnsupportedEncodingException {

            try{

                logger.info("Hlllo");
                if (customerDetails.getMobileNumber().isEmpty() || customerDetails.getMobileNumber()==null)
                    return new ResponseEntity<>("Enter mobile number", HttpStatus.UNPROCESSABLE_ENTITY);

                String mobileNumber = null;
                if (customerDetails.getMobileNumber().startsWith("0")) {
                    mobileNumber = customerDetails.getMobileNumber().substring(1);
                }else{
                     mobileNumber = customerDetails.getMobileNumber();
                }

                if (!customCustomerService.isValidMobileNumber(mobileNumber)) {
                    return ResponseEntity.badRequest().body("Invalid mobile number");
                }

                String countryCode = null;
                if (customerDetails.getCountryCode() == null || customerDetails.getCountryCode().isEmpty()) {

                    countryCode = Constant.COUNTRY_CODE;
                }else{
                    countryCode = customerDetails.getCountryCode();

                }
                twilioService.setotp(mobileNumber, countryCode);


                ResponseEntity<String> otpResponse = twilioService.sendOtpToMobile(mobileNumber, countryCode);
                return otpResponse;
            }catch (Exception e){
                exceptionHandling.handleException(e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());

            }

    }


    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody CustomCustomer customerDetails,@RequestParam("otpEntered") String otpEntered, HttpSession session) {
        try {

            if(customerDetails.getMobileNumber()!=null){

                customerDetails.setMobileNumber(customerDetails.getMobileNumber());

            } else if (customerDetails.getUsername()!=null) {
                if (customerService == null) {
                    return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                Customer customer = customerService.readCustomerByUsername(customerDetails.getUsername());
                if (customer == null) {
                    return new ResponseEntity<>("No records found for the provided username.", HttpStatus.NOT_FOUND);
                }
                CustomCustomer customCustomer = em.find(CustomCustomer.class, customer.getId());

                if (customCustomer != null) {
                    customerDetails.setMobileNumber(customCustomer.getMobileNumber());
                } else {
                    return new ResponseEntity<>("No records found", HttpStatus.NO_CONTENT);
                }

            }else{
                return new ResponseEntity<>("Invalid data", HttpStatus.INTERNAL_SERVER_ERROR);
            }


            if (!customCustomerService.isValidMobileNumber(customerDetails.getMobileNumber())) {
                return new ResponseEntity<>("Invalid mobile number", HttpStatus.NOT_FOUND);

            }

            if (otpEntered == null || otpEntered.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("OTP can not be empty");
            }

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(customerDetails.getMobileNumber(), customerDetails.getCountryCode());

            String storedOtp = existingCustomer.getOtp();
            System.out.println("Entered OTP: " + otpEntered + ", storedOtp OTP: " + storedOtp + " session " + customerDetails.getMobileNumber());
            if (otpEntered.equals(storedOtp)) {
                String tokenKey = "authToken_" + customerDetails.getMobileNumber();
                String existingToken = (String) session.getAttribute(tokenKey);
                System.out.println(existingToken + " existingToken");
                if (existingToken!= null && jwtUtil.validateToken(existingToken, customCustomerService)) {
                    return ResponseEntity.ok(new AuthResponse(existingToken));
                } else {
                    String newToken = jwtUtil.generateToken(customerDetails.getMobileNumber(),"USER");
                    session.setAttribute(tokenKey, newToken);
                    return ResponseEntity.ok(new AuthResponse(newToken));
                }

            } else {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error saving customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public static class AuthResponse {
        private String token;

        public AuthResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }

}