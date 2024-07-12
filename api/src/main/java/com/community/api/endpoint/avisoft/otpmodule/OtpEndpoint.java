package com.community.api.endpoint.avisoft.otpmodule;

import com.community.api.services.ExceptionHandlingImplement;
import com.community.api.services.TwilioService;
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

        String encodedCountryCode = URLEncoder.encode(countrycode, "UTF-8");

        String completeMobileNumber = encodedCountryCode + mobileNumber;

        ResponseEntity<String> otpResponse = twilioService.sendOtpToMobile(completeMobileNumber);
        System.out.println(otpResponse.getBody() + "  otpResponse  send-otp ");

        if (otpResponse.getStatusCode() == HttpStatus.OK) {
            session.setAttribute("expectedOtp", otpResponse.getBody());
            // session.setAttribute("mobileNumber", mobileNumber);

            return ResponseEntity.ok("OTP sent successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to send OTP");
        }
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
            session.removeAttribute("mobileNumber");
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


}