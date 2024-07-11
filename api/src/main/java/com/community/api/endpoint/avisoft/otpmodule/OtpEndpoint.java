package com.community.api.endpoint.avisoft.otpmodule;

import com.community.api.services.FirebaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/otp")
public class OtpEndpoint {

    private final FirebaseService firebaseService;

    public OtpEndpoint(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> requestOtp(@RequestParam("mobileNumber") String mobileNumber) {

        if (!isValidMobileNumber(mobileNumber)) {
            return ResponseEntity.badRequest().body("Invalid mobile number");
        }

        String otpResponse = firebaseService.sendOtpToMobile(mobileNumber);
        if (otpResponse != null) {
            return ResponseEntity.ok("OTP sent successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to send OTP");
        }
    }

    private boolean isValidMobileNumber(String mobileNumber) {
        String mobileNumberPattern = "^\\+?\\d{10,13}$";
        return Pattern.compile(mobileNumberPattern).matcher(mobileNumber).matches();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam("mobileNumber") String mobileNumber, @RequestParam("otp") String otp) {
        if (!isValidMobileNumber(mobileNumber)) {
            return ResponseEntity.badRequest().body("Invalid mobile number");
        }

        if (otp == null || otp.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        String verifyResponse = firebaseService.verifyOtp(mobileNumber, otp);
        if (verifyResponse != null && verifyResponse.equals("success")) {

            
            return ResponseEntity.ok("OTP verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }
    }

    private void logError(Exception e) {

        System.err.println("Error: " + e.getMessage());
    }
}