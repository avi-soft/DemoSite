package com.community.api.endpoint.avisoft.controller.otpmodule;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.TwilioService;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@RestController
@RequestMapping("/otp")
public class OtpEndpoint {

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    private final TwilioService twilioService;

    @Autowired
    private CustomCustomerService customCustomerService;
    @Autowired
    private JwtUtil jwtUtil;
    public OtpEndpoint( TwilioService twilioService) {

        this.twilioService = twilioService;
    }

    @Autowired
    private CustomerService customerService;


    @Autowired
    private EntityManager entityManager;


    @PostMapping("/send-otp")
    public ResponseEntity<String> sendtOtp(@RequestBody CustomCustomer customerDetails,HttpSession session) throws UnsupportedEncodingException {

            try{
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
                    String encodedCountryCode = URLEncoder.encode(countryCode, "UTF-8");
                    countryCode = encodedCountryCode;

                }
                twilioService.setotp(mobileNumber, countryCode);


                System.out.println("mobileNumber: " + mobileNumber);
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

            if (customerDetails.getMobileNumber().isEmpty() || customerDetails.getMobileNumber()==null)
                return new ResponseEntity<>("Enter mobile number", HttpStatus.UNPROCESSABLE_ENTITY);



            if (otpEntered == null || otpEntered.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid OTP");
            }

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(customerDetails.getMobileNumber(), null);

            String storedOtp = existingCustomer.getOtp();
            System.out.println("Entered OTP: " + otpEntered + ", storedOtp OTP: " + storedOtp + " session " + customerDetails.getMobileNumber());
            if (otpEntered.equals(storedOtp)) {
                Boolean existingCustomerFlag =  twilioService.setotp(customerDetails.getMobileNumber(), existingCustomer.getCountryCode());
                    if (customerService == null) {
                        return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                   /* String token = jwtUtil.generateToken(customerDetails.getMobileNumber());
                    return ResponseEntity.ok(new AuthResponse(token));*/
                return ResponseEntity.ok("OTP verified successfully ");



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