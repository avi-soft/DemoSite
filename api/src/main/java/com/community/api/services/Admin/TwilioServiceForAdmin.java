package com.community.api.services.Admin;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomAdmin;
import com.community.api.services.ApiConstants;
import com.community.api.services.ResponseService;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

    @Service
    public class TwilioServiceForAdmin {

        @Autowired
        private ExceptionHandlingImplement exceptionHandling;

        @Value("${twilio.accountSid}")
        private String accountSid;

        @Value("${twilio.authToken}")
        private String authToken;

        @Value("${twilio.phoneNumber}")
        private String twilioPhoneNumber;

        @Autowired
        private AdminService adminService;  // Service to manage ServiceProviderEntity
        @Autowired
        private ResponseService responseService;
        @Autowired
        private EntityManager entityManager;

        @Transactional
        public ResponseEntity<?> sendOtpToMobileForAdmin(String mobileNumber, String countryCode) {

            if (mobileNumber == null || mobileNumber.isEmpty()) {
                return ResponseEntity.badRequest().body("Mobile number cannot be null or empty");
            }

            try {
                Twilio.init(accountSid, authToken);
                String completeMobileNumber = countryCode + mobileNumber;
                String otp = generateOTPForAdmin();


                CustomAdmin existingAdmin = adminService.findAdminByPhone(mobileNumber,countryCode);

                if (existingAdmin == null) {
                    System.out.println(existingAdmin + " existingAdmin");
                    existingAdmin = new CustomAdmin();
                    // Populate other necessary fields
                    existingAdmin.setCountry_code(countryCode);
                    existingAdmin.setMobileNumber(mobileNumber);
                    existingAdmin.setOtp(otp);
                    entityManager.persist(existingAdmin);
                } else {
                    System.out.println(existingAdmin + " existingAdmin");
                    existingAdmin.setOtp(null);
                    existingAdmin.setOtp(otp);
                    entityManager.merge(existingAdmin);
                }
                Map<String,Object> details=new HashMap<>();
                // details.put("message",ApiConstants.OTP_SENT_SUCCESSFULLY);
                details.put("status", ApiConstants.STATUS_SUCCESS);
                details.put("otp",otp);
                return responseService.generateSuccessResponse(ApiConstants.OTP_SENT_SUCCESSFULLY,details, HttpStatus.OK);

            } catch (ApiException e) {
                exceptionHandling.handleApiException(e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
            } catch (Exception e) {
                exceptionHandling.handleException(e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
            }
        }

        private synchronized String generateOTPForAdmin() {
            Random random = new Random();
            int otp = 1000 + random.nextInt(8999);
            return String.valueOf(otp);
        }

        @Transactional
        public boolean setOtpForAdmin(String mobileNumber, String countryCode) {
            CustomAdmin existingAdmin = adminService.findAdminByPhone(mobileNumber,countryCode);

            if (existingAdmin != null) {
                String storedOtp = existingAdmin.getOtp();
                if (storedOtp != null) {
                    existingAdmin.setOtp(null);
                    entityManager.merge(existingAdmin);
                    return true;
                }
            }
            return false;
        }

    }
