package com.community.api.endpoint.avisoft.otpmodule;

import com.community.api.services.ExceptionHandlingImplement;
import com.community.api.services.TwilioService;
import org.broadleafcommerce.profile.core.dao.PhoneDao;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.Phone;
import org.broadleafcommerce.profile.core.service.CustomerPhoneService;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.broadleafcommerce.profile.core.service.PhoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/phone")
public class OtpEndpoint {

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PhoneService phoneService;


    private final TwilioService twilioService;

    public OtpEndpoint(TwilioService twilioService) {
        this.twilioService = twilioService;
    }



    @GetMapping("/send-otp")
    public ResponseEntity<String> requestOtp(@RequestParam("mobileNumber") String mobileNumber,
                                             @RequestParam("countrycode") String countrycode,
                                             HttpSession session) throws UnsupportedEncodingException {
        if (!isValidMobileNumber(mobileNumber)) {
            return ResponseEntity.badRequest().body("Invalid mobile number");
        }

       return   twilioService.sendOTPFunction(mobileNumber,countrycode,session);

    }


    private boolean isValidMobileNumber(String mobileNumber) {
        String mobileNumberPattern = "^\\+?\\d{10,13}$";
        return Pattern.compile(mobileNumberPattern).matcher(mobileNumber).matches();
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOTP(@RequestParam("otpEntered") String otpEntered, HttpSession session) {
        String expectedOtp = (String) session.getAttribute("expectedOtp");
        /*     String mobileNumber = (String) session.getAttribute("mobileNumber");*/

        if (otpEntered == null || otpEntered.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        if (otpEntered.equals(expectedOtp)) {
            session.removeAttribute("expectedOtp");
//            session.removeAttribute("mobileNumber");
            return ResponseEntity.ok("OTP verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }
    }

    @GetMapping("/catcherror")
    public ResponseEntity<String> catcherror() {
        try {
            int x = 5 / 0;
        } catch (Exception e) {

            String errorMessage = exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);

        }
        return ResponseEntity.ok("Success");

    }


/*    @GetMapping("/verifyandlogin")
    public ResponseEntity<String> verifyandlogin(@RequestParam("identifier") String identifier,
                                                 @RequestParam("countrycode") String countrycode,
                                                 @RequestParam("otpOrPassword") String otpOrPassword,
                                                 HttpSession session) throws UnsupportedEncodingException {
        if (isValidMobileNumber(identifier)) {
            return loginWithMobileNumber(identifier, countrycode, otpOrPassword, session);
        } else {
            return loginWithUsername(identifier, otpOrPassword, session);
        }
    }*/

   /* private ResponseEntity<String> loginWithMobileNumber(String mobileNumber, String countrycode, String otpOrPassword, HttpSession session) throws UnsupportedEncodingException {
        Customer customer = customerService.readCustomerByUsername(username);
        User user = userRepository.findByMobileNumber(mobileNumber);
        if (customer != null) {
            if (!isValidMobileNumber(mobileNumber)) {
                return ResponseEntity.badRequest().body("Invalid mobile number");
            }

            ResponseEntity<String> otpResponse = twilioService.sendOTPFunction(mobileNumber, countrycode, session);
            if (otpResponse.getStatusCode() == HttpStatus.OK) {

                if (user.getOtp().equals(otpOrPassword)) {

                    return ResponseEntity.ok("Login successful");
                } else {

                    return ResponseEntity.badRequest().body("Invalid" +
                            " OTP");
                }
            } else {

                if (customerService.isPasswordValid().equals(otpOrPassword)) {

                    return ResponseEntity.ok("Login successful");
                } else {
                    return ResponseEntity.badRequest().body("Invalid password");
                }
            }
        } else {
            return ResponseEntity.badRequest().body("Mobile number not found");
        }
    }

    private ResponseEntity<String> loginWithUsername(String username, String otpOrPassword, HttpSession session) {
        Customer customer = customerService.readCustomerByUsername(username);

        if (customer != null) {
                if (otpOrPassword != null && otpOrPassword.length() == 6) {

                    if (customer.getOtp().equals(otpOrPassword)) {

                        return ResponseEntity.ok("Login successful");
                    } else {

                        return ResponseEntity.badRequest().body("Invalid OTP");
                    }
                } else {

                    if (customer.getPassword().equals(otpOrPassword)) {

                        return ResponseEntity.ok("Login successful");
                    } else {

                        return ResponseEntity.badRequest().body("Invalid password");
                    }
                }

        } else {
            return ResponseEntity.badRequest().body("Username not found");
        }
    }*/

}