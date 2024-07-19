package com.community.api.endpoint.avisoft.controller.otpmodule;
import com.community.api.services.CustomCustomerService;
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

    @Autowired
    private CustomCustomerService customCustomerService;

    public OtpEndpoint( TwilioService twilioService) {

        this.twilioService = twilioService;
    }

    @Autowired
   private HttpSession httpSession;

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
/*        if (otpResponse.getStatusCode() == HttpStatus.OK) {
            session.setAttribute("expectedOtp", httpSession.getAttribute("expectedOtp"));
            return ResponseEntity.ok("OTP sent successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to send OTP");
        }*/
       /* if (otpResponse.getStatusCode() == HttpStatus.OK) {
            session.setAttribute("expectedOtp", otpResponse.getBody());

            return ResponseEntity.ok("OTP sent successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to send OTP");
        }*/
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOTP(@RequestParam("otpEntered") String otpEntered, HttpSession session) {

        String expectedOtp = (String) session.getAttribute("expectedOtp");
        System.out.println("Entered OTP: " + otpEntered + ", Expected OTP: " + expectedOtp);

        if (otpEntered == null || otpEntered.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        System.out.println("Entered OTP: " + otpEntered + ", Expected OTP: " + expectedOtp + " session " + session.getAttribute("expectedOtp"));

        if (otpEntered.equals(expectedOtp)) {
            session.removeAttribute("expectedOtp");
            return ResponseEntity.ok("OTP verified successfully");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
        }
    }

}