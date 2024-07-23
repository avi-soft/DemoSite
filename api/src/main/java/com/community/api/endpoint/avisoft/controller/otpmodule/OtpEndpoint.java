package com.community.api.endpoint.avisoft.controller.otpmodule;
import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.TwilioService;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    @GetMapping("/send-otp")
    public ResponseEntity<String> sendtOtp(@RequestParam("mobileNumber") String mobileNumber, @RequestParam(value = "countryCode", required = false) String countryCode,

                                             HttpSession session) throws UnsupportedEncodingException {

        if (mobileNumber.startsWith("0")) {
            mobileNumber = mobileNumber.substring(1);
        }

        if (!customCustomerService.isValidMobileNumber(mobileNumber)) {
            return ResponseEntity.badRequest().body("Invalid mobile number");
        }
        if (countryCode == null || countryCode.isEmpty()) {

            countryCode = Constant.COUNTRY_CODE;
        }else{
            String encodedCountryCode = URLEncoder.encode(countryCode, "UTF-8");
            countryCode = encodedCountryCode;

        }
        String completeMobileNumber = countryCode + mobileNumber;

        if (session.getAttribute("expectedOtp_" + completeMobileNumber) != null) {
            session.removeAttribute("expectedOtp_" + completeMobileNumber);
            session.removeAttribute("mobileNumber_" + mobileNumber);
            session.removeAttribute("country_code_" + countryCode);

        }

        ResponseEntity<String> otpResponse = twilioService.sendOtpToMobile(mobileNumber, countryCode);
        return otpResponse;

    }

    @Transactional
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestParam("otpEntered") String otpEntered, HttpSession session) {

        String mobileNumber = (String) session.getAttribute("mobileNumber");
        String countryCode = (String) session.getAttribute("countryCode");


        if (otpEntered == null || otpEntered.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }
        String completeMobileNumber = countryCode + mobileNumber;
        String expectedOtp = (String) session.getAttribute("expectedOtp_" + completeMobileNumber);
        System.out.println("Entered OTP: " + otpEntered + ", Expected OTP: " + expectedOtp + " session " + mobileNumber);

        if (otpEntered.equals(expectedOtp)) {
            try {

                if (customerService == null) {
                    return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
                }

                CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber,countryCode);
                System.out.println("existingCustomer : " + existingCustomer );
                if(existingCustomer == null){
                    CustomCustomer customerDetails = new CustomCustomer();
                    customerDetails.setId(customerService.findNextCustomerId());
                    customerDetails.setCountryCode(countryCode);
                    customerDetails.setMobileNumber(mobileNumber);

                    session.removeAttribute("expectedOtp_" + completeMobileNumber);
                    session.removeAttribute("mobileNumber");
                    session.removeAttribute("countryCode");
                    entityManager.persist(customerDetails);

                }
                String token = jwtUtil.generateToken(mobileNumber);
                return ResponseEntity.ok(new AuthResponse(token));

            } catch (Exception e) {
                exceptionHandling.handleException(e);
                return new ResponseEntity<>("Error saving customer", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
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