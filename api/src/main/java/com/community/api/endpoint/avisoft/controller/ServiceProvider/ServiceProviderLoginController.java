package com.community.api.endpoint.avisoft.controller.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.RateLimiterService;
import com.community.api.services.TwilioServiceForServiceProvider;
import com.community.api.services.exception.ExceptionHandlingImplement;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/service-provider/otp")
public class ServiceProviderLoginController {

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    private ServiceProviderServiceImpl serviceProviderService;
    private final TwilioServiceForServiceProvider twilioService;
    private final RateLimiterService rateLimiterService;
    private EntityManager entityManager;

    @Autowired
    public ServiceProviderLoginController(TwilioServiceForServiceProvider twilioService,EntityManager entityManager, RateLimiterService rateLimiterService, ServiceProviderServiceImpl serviceProviderService) {
        this.twilioService = twilioService;
        this.rateLimiterService = rateLimiterService;
        this.serviceProviderService = serviceProviderService;
        this.entityManager=entityManager;
    }
    @PostMapping("login-with-password")
    public ResponseEntity<?> loginWithPassword(@RequestBody Map<String, Object> serviceProviderDetails, HttpSession session) {
        try {
            String primaryMobileNumber = (String) serviceProviderDetails.get("primaryMobileNumber");
            String username = (String) serviceProviderDetails.get("username");
            String password = (String) serviceProviderDetails.get("password");
            String countryCode = (String) serviceProviderDetails.getOrDefault("countryCode", Constant.COUNTRY_CODE);
            // Check for empty password
            if (password == null || password.isEmpty()) {
                return new ResponseEntity<>("Password cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (primaryMobileNumber != null && !primaryMobileNumber.isEmpty()) {
                return serviceProviderService.authenticateByPhone(primaryMobileNumber, countryCode, password);
            } else if (username != null && !username.isEmpty()) {
                return serviceProviderService.authenticateByUsername(username, password);
            } else {
                return new ResponseEntity<>("Empty Phone Number or username", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
    @PostMapping("username-otp-login")
    public ResponseEntity<?>loginWithUsernameAndOTP(@RequestBody Map<String, Object> serviceProviderDetails,HttpSession seesion)
    {
        try {
            String username = (String) serviceProviderDetails.get("username");
            if (username == null || username.isEmpty()) {
                return new ResponseEntity<>("Empty Credentials", HttpStatus.BAD_REQUEST);
            }
            ServiceProviderEntity existingServiceProivder = serviceProviderService.findServiceProviderByUserName(username);
            if (existingServiceProivder == null)
                return new ResponseEntity<>("No records found", HttpStatus.NOT_FOUND);
            if (existingServiceProivder.getPrimary_mobile_number() == null) {
                return new ResponseEntity<>("No mobile Number registerd for this account", HttpStatus.NOT_FOUND);
            }
            Map<String, Object> updatedDetails = new HashMap<>();
            updatedDetails.put("primaryMobileNumber", existingServiceProivder.getPrimary_mobile_number());
            return new ResponseEntity<>(sendOtp(updatedDetails, seesion),HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
        }
    }
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody Map<String, Object> serviceProviderDetails, HttpSession session) throws UnsupportedEncodingException {
        try {
            if (((String)serviceProviderDetails.get("primaryMobileNumber")).isEmpty()) {
                return new ResponseEntity<>("Enter mobile number", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            String mobileNumber=((String)serviceProviderDetails.get("primaryMobileNumber"));
            mobileNumber = mobileNumber.startsWith("0")
                    ? mobileNumber.substring(1)
                    : mobileNumber;
            //ServiceProviderEntity existingServiceProvider = serviceProviderService.findServiceProviderByPhoneWithOtp(mobileNumber);
            String countryCode = (serviceProviderDetails.get("countryCode")) == null || ((String)serviceProviderDetails.get("countryCode")).isEmpty()
                    ? Constant.COUNTRY_CODE
                    : ((String)serviceProviderDetails.get("countryCode"));

            Bucket bucket = rateLimiterService.resolveBucket(mobileNumber, "/service-provider/otp/send-otp");
            if (bucket.tryConsume(1)) {
                if (!serviceProviderService.isValidMobileNumber(mobileNumber)) {
                    return ResponseEntity.badRequest().body("Invalid mobile number");
                }

                ResponseEntity<String> otpResponse = twilioService.sendOtpToMobile(mobileNumber,countryCode);
                return otpResponse;
            } else {
                return ResponseEntity.ok("You can send OTP only once in 1 minute");
            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
        }
    }
    @Transactional
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String ,Object> serviceProviderDetails, @RequestParam("otpEntered") String otpEntered, HttpSession session, HttpServletRequest request) {
        try {

            String mobileNumber = ((String)serviceProviderDetails.get("primaryMobileNumber"));
            String countryCode = ((String)serviceProviderDetails.get("countryCode")) == null || ((String)serviceProviderDetails.get("primaryMobileNumber")).isEmpty()
                    ? Constant.COUNTRY_CODE
                    : (String)serviceProviderDetails.get("primaryMobileNumber");
            ServiceProviderEntity existingServiceProvider = serviceProviderService.findServiceProviderByPhone(mobileNumber,countryCode);

            if (!serviceProviderService.isValidMobileNumber(mobileNumber)) {
                return new ResponseEntity<>("Invalid mobile number", HttpStatus.NOT_FOUND);
            }

            if (otpEntered == null || otpEntered.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("OTP cannot be empty");
            }

            String storedOtp = existingServiceProvider.getOtp();
            if (otpEntered.equals(storedOtp)) {
                existingServiceProvider.setOtp(null);
                entityManager.merge(existingServiceProvider);
                // Create and return JWT token as per your requirement
                return ResponseEntity.ok(existingServiceProvider);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error verifying OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
