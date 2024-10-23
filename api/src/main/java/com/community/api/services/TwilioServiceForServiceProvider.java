package com.community.api.services;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.services.ServiceProvider.ServiceProviderService;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
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
public class TwilioServiceForServiceProvider {

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.phoneNumber}")
    private String twilioPhoneNumber;

    @Autowired
    private ServiceProviderServiceImpl serviceProviderService;  // Service to manage ServiceProviderEntity
    @Autowired
    private ResponseService responseService;
    @Autowired
    private EntityManager entityManager;

    @Transactional
    public ResponseEntity<?> sendOtpToMobile(String mobileNumber, String countryCode) {

        if (mobileNumber == null || mobileNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Mobile number cannot be null or empty");
        }

        try {
            Twilio.init(accountSid, authToken);
            String otp = generateOTP();
            ServiceProviderEntity existingServiceProvider = serviceProviderService.findServiceProviderByPhone(mobileNumber,countryCode);

            if (existingServiceProvider == null) {
                existingServiceProvider = new ServiceProviderEntity();
                existingServiceProvider.setCountry_code(countryCode);
                existingServiceProvider.setMobileNumber(mobileNumber);
                existingServiceProvider.setOtp(otp);
                entityManager.persist(existingServiceProvider);
            } else {
                existingServiceProvider.setOtp(null);
                existingServiceProvider.setOtp(otp);
                entityManager.merge(existingServiceProvider);
            }

            String maskedNumber = this.genereateMaskednumber(mobileNumber);
            return responseService.generateSuccessResponse("Otp has been sent successfully on " + maskedNumber,otp,HttpStatus.OK);

        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
        }
    }
    public synchronized String genereateMaskednumber(String mobileNumber){
        String lastFourDigits = mobileNumber.substring(mobileNumber.length() - 4);

        int numXs = mobileNumber.length() - 4;

        StringBuilder maskBuilder = new StringBuilder();
        for (int i = 0; i < numXs; i++) {
            maskBuilder.append('x');
        }
        String mask = maskBuilder.toString();

        String maskedNumber = mask + lastFourDigits;
        return  maskedNumber;
    }

    private synchronized String generateOTP() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(8999);
        return String.valueOf(otp);
    }

    @Transactional
    public boolean setOtp(String mobileNumber, String countryCode) {
        ServiceProviderEntity existingServiceProvider = serviceProviderService.findServiceProviderByPhone(mobileNumber,countryCode);

        if (existingServiceProvider != null) {
            String storedOtp = existingServiceProvider.getOtp();
            if (storedOtp != null) {
                existingServiceProvider.setOtp(null);
                entityManager.merge(existingServiceProvider);
                return true;
            }
        }
        return false;
    }
}
