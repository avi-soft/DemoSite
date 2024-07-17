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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

    public ResponseEntity<String> sendOtpToMobile(String mobileNumber) {
        if (mobileNumber == null || mobileNumber.isEmpty()) {
            throw new IllegalArgumentException("Mobile number cannot be null or empty");
        }

        try {
            Twilio.init(accountSid, authToken);

            String otp = generateOTP();

          Message message = Message.creator(
                            new PhoneNumber(mobileNumber),
                            new PhoneNumber(twilioPhoneNumber),
                          otp)
                    .create();

            return ResponseEntity.ok(otp);

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

    public ResponseEntity<String> sendOTPFunction(String mobileNumber, String countryCode, HttpSession session) throws UnsupportedEncodingException {
        String encodedCountryCode = URLEncoder.encode(countryCode, "UTF-8");
        String completeMobileNumber = encodedCountryCode + mobileNumber;
        System.out.println(completeMobileNumber + "  encodedCountryCode " +encodedCountryCode);
        ResponseEntity<String> otpResponse = this.sendOtpToMobile(completeMobileNumber);
        System.out.println(otpResponse.getBody() + "  otpResponse  send-otp ");


        if (otpResponse.getStatusCode() == HttpStatus.OK) {
            session.setAttribute("expectedOtp", otpResponse.getBody());
            return ResponseEntity.ok("OTP has been sent on your number " + mobileNumber);
        } else {
            return ResponseEntity.internalServerError().body("Failed to send OTP on " + mobileNumber);
        }

    }


    private synchronized String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

}
