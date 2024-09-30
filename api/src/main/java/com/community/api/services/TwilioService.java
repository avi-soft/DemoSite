package com.community.api.services;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.ServiceProviderAddress;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class TwilioService {

    private ExceptionHandlingImplement exceptionHandling;

    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.phoneNumber}")
    private String twilioPhoneNumber;

    private CustomCustomerService customCustomerService;
    private EntityManager entityManager;
    private HttpSession httpSession;
    @Autowired
    private  ServiceProviderServiceImpl serviceProviderService;
    @Autowired

    private CustomerService customerService;

    public TwilioService(ExceptionHandlingImplement exceptionHandlingImplement,CustomCustomerService customCustomerService,EntityManager entityManager,HttpSession httpSession,CustomerService customerService)
    {
         this.exceptionHandling = exceptionHandlingImplement;
         this.customCustomerService = customCustomerService;
         this.entityManager = entityManager;
         this.httpSession= httpSession;
         this.customerService=customerService;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> sendOtpToMobile(String mobileNumber, String countryCode) {

        if (mobileNumber == null || mobileNumber.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", ApiConstants.STATUS_ERROR,
                    "status_code", HttpStatus.BAD_REQUEST,
                    "message", ApiConstants.MOBILE_NUMBER_NULL_OR_EMPTY
            ));
        }

        try {
            Twilio.init(accountSid, authToken);
            String completeMobileNumber = countryCode + mobileNumber;
            String otp = generateOTP();

            // Uncomment the code to send OTP via SMS
            /*
            Message message = Message.creator(
                new PhoneNumber(completeMobileNumber),
                new PhoneNumber(twilioPhoneNumber),
                otp
            ).create();
            */

            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);
            ServiceProviderEntity serviceProvider = serviceProviderService.findServiceProviderByPhone(mobileNumber, countryCode);
            String maskedNumber = this.genereateMaskednumber(mobileNumber);
            if (existingCustomer == null && serviceProvider == null) {
                CustomCustomer customerDetails = new CustomCustomer();
                customerDetails.setId(customerService.findNextCustomerId());
                customerDetails.setCountryCode(countryCode);
                customerDetails.setMobileNumber(mobileNumber);
                customerDetails.setOtp(otp);
                entityManager.persist(customerDetails);
                return ResponseEntity.ok(Map.of(
                        "otp", otp,
                         "message", "Otp has been sent successfully on " + maskedNumber
                ));
            } else if (serviceProvider != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "status_code", HttpStatus.BAD_REQUEST,
                        "message", ApiConstants.NUMBER_ALREADY_REGISTERED_SERVICE_PROVIDER
                ));
            } else {
                existingCustomer.setOtp(otp);
                entityManager.merge(existingCustomer);
                return ResponseEntity.ok(Map.of(

                        "otp", otp,
                         "message", "Otp has been sent successfully on " + maskedNumber
                ));
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "message", ApiConstants.UNAUTHORIZED_ACCESS,
                        "status_code", HttpStatus.UNAUTHORIZED
                ));
            } else {
                exceptionHandling.handleHttpClientErrorException(e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "message", ApiConstants.INTERNAL_SERVER_ERROR,
                        "status_code", HttpStatus.INTERNAL_SERVER_ERROR
                ));
            }
        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", ApiConstants.STATUS_ERROR,
                    "message", ApiConstants.ERROR_SENDING_OTP + e.getMessage(),
                    "status_code", HttpStatus.INTERNAL_SERVER_ERROR
            ));
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", ApiConstants.STATUS_ERROR,
                    "message", ApiConstants.ERROR_SENDING_OTP + e.getMessage(),
                    "status_code", HttpStatus.INTERNAL_SERVER_ERROR
            ));
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
    public boolean setotp(String mobileNumber, String countryCode) {
        CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);

        if(existingCustomer!=null){
            String storedOtp = existingCustomer.getOtp();
            if(storedOtp!=null){
                existingCustomer.setOtp(null);
                entityManager.merge(existingCustomer);
                return true;
            }
        }
        return false;
    }
}

