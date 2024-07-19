package com.community.api.endpoint.avisoft.controller.otpmodule;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.TwilioService;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.broadleafcommerce.profile.web.core.service.login.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/phone")
public class OtpEndpoint {

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    public static final String COUNTRY_CODE = "+91";

    private final TwilioService twilioService;

    public OtpEndpoint( TwilioService twilioService) {

        this.twilioService = twilioService;
    }

    @GetMapping("/send-otp")
    public ResponseEntity<String> sendtOtp(@RequestParam("mobileNumber") String mobileNumber, @RequestParam(value = "countryCode", required = false) String countryCode,

                                             HttpSession session) throws UnsupportedEncodingException {
        if (!isValidMobileNumber(mobileNumber)) {
            return ResponseEntity.badRequest().body("Invalid mobile number");
        }
        if (countryCode == null || countryCode.isEmpty()) {
            countryCode = COUNTRY_CODE;
        }
       return twilioService.sendOTPFunction(mobileNumber,countryCode,session);

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
            
            String sessionId = generateSessionId();
            session.setAttribute("sessionId", sessionId);
            return ResponseEntity.ok("OTP verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }
    }


    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}