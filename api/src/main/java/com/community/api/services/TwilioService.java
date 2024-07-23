package com.community.api.services;

import com.community.api.services.exception.ExceptionHandlingImplement;
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

import javax.servlet.http.HttpSession;
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

            httpSession.setAttribute("expectedOtp",otp);
            httpSession.setAttribute("mobileNumber",mobileNumber);
           System.out.println("OTP set in session: " + otp);
           return ResponseEntity.ok("OTP has been sent successfully "+otp);


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

}
