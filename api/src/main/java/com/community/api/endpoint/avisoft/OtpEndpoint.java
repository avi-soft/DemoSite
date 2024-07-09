package com.community.api.endpoint.avisoft;

import com.community.api.services.FirebaseService;
import org.codehaus.jettison.json.JSONException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class OtpEndpoint {

    @PostMapping("/request-otp")
    public ResponseEntity<String> requestOtp(@RequestParam("mobileNumber") String mobileNumber) {
        // Call Firebase API to send OTP to user's mobile number
        FirebaseService firebaseService = new FirebaseService();
        try {
            String otpResponse = firebaseService.sendOtpToMobile(mobileNumber);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Returning a success response
        return ResponseEntity.ok("OTP sent successfully");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam("mobileNumber") String mobileNumber, @RequestParam("otp") String otp) {
        // Call Firebase API to verify OTP
        FirebaseService firebaseService = new FirebaseService();
        String verifyResponse = null;
        try {
            verifyResponse = firebaseService.verifyOtp(mobileNumber, otp);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Return a success or failure response based on Firebase's response
        if (verifyResponse.equals("success")) {
            return ResponseEntity.ok("OTP verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }
    }
}
