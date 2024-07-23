package com.community.api.endpoint.avisoft.controller.otpmodule;
import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.endpoint.customer.CustomerDTO;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.TwilioService;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/phone")
public class OtpEndpoint {

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    public static final String COUNTRY_CODE = "+91";

    private final TwilioService twilioService;

    @Autowired
    private CustomCustomerService customCustomerService;

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
        if (!customCustomerService.isValidMobileNumber(mobileNumber)) {
            return ResponseEntity.badRequest().body("Invalid mobile number");
        }
        if (countryCode == null || countryCode.isEmpty()) {
            countryCode = COUNTRY_CODE;
        }

        ResponseEntity<String> otpResponse = twilioService.sendOtpToMobile(mobileNumber, countryCode);
        return otpResponse;

    }


    @Transactional
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOTP(@RequestParam("otpEntered") String otpEntered, HttpSession session) {

        String expectedOtp = (String) session.getAttribute("expectedOtp");
        String mobileNumber = (String) session.getAttribute("mobileNumber");
        System.out.println("Entered OTP: " + otpEntered + ", Expected OTP: " + expectedOtp);

        if (otpEntered == null || otpEntered.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        System.out.println("Entered OTP: " + otpEntered + ", Expected OTP: " + expectedOtp + " session " + mobileNumber);

        if (otpEntered.equals(expectedOtp)) {
            session.removeAttribute("expectedOtp");
            try {

                if (customerService == null) {
                    return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
                }

                CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber);
                System.out.println("existingCustomer : " + existingCustomer );
                if(existingCustomer == null){
                    CustomCustomer customerDetails = new CustomCustomer();
                    customerDetails.setId(customerService.findNextCustomerId());
                    customerDetails.setMobileNumber(mobileNumber);
                    System.out.println("customerDetails : " + customerDetails );
                    session.removeAttribute("mobileNumber");
                    entityManager.persist(customerDetails);
                    return ResponseEntity.ok("OTP verified and Customer Created Successfully "+customerDetails.getId());
                }else{
                    System.out.println("customerDetailselse : "  );
                    return ResponseEntity.ok("OTP verified Successfully");
                }


            } catch (Exception e) {
                exceptionHandling.handleException(e);
                return new ResponseEntity<>("Error saving customer", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
        }
    }

}