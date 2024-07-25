package com.community.api.services;

import com.community.api.endpoint.customer.CustomCustomer;
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

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.phoneNumber}")
    private String twilioPhoneNumber;

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private CustomCustomerService customCustomerService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CustomerService customerService;

    @Transactional
    public ResponseEntity<String> sendOtpToMobile(String mobileNumber, String countryCode) {

        if (mobileNumber == null || mobileNumber.isEmpty()) {
            throw new IllegalArgumentException("Mobile number cannot be null or empty");
        }

        try {
            Twilio.init(accountSid, authToken);
            String completeMobileNumber = countryCode + mobileNumber;
            String otp = generateOTP();

            System.out.println(completeMobileNumber + " completeMobileNumber");

           Message message = Message.creator(
                            new PhoneNumber(completeMobileNumber),
                            new PhoneNumber(twilioPhoneNumber),
                            otp)
                    .create();


            CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber,countryCode);
            System.out.println("existingCustomer : " + existingCustomer );
            if(existingCustomer == null){
                CustomCustomer customerDetails = new CustomCustomer();
                customerDetails.setId(customerService.findNextCustomerId());
                customerDetails.setCountryCode(countryCode);
                customerDetails.setMobileNumber(mobileNumber);
                customerDetails.setOtp(otp);
                entityManager.persist(customerDetails);

            }else{
                existingCustomer.setOtp(otp);
                entityManager.merge(existingCustomer);
            }

           return ResponseEntity.ok("OTP has been sent successfully " + otp);


        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access: Please check your API key");
            } else {
                exceptionHandling.handleHttpClientErrorException(e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred");
            }
        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
        }
    }

    private synchronized String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }


    @Transactional
    public boolean setotp(String mobileNumber, String countryCode) {
        CustomCustomer existingCustomer = customCustomerService.findCustomCustomerByPhone(mobileNumber, countryCode);

        if(existingCustomer!=null){
            String storedOtp = existingCustomer.getOtp();
            System.out.println("storedOtp: " + storedOtp);
                    if(storedOtp!=null){
                        existingCustomer.setOtp(null);
                        entityManager.merge(existingCustomer);
                        return true;
                    }
        }
        return false;
    }
}
