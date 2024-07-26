package com.community.api.component;

import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {


    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private CustomCustomerService customCustomerService;

    private Key secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

   public String generateToken(String phoneNumber, String countryCode) {

        System.out.println(secretKey + " secretKey");
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .claim("phoneNumber", phoneNumber)
                .claim("countryCode", countryCode)
                .setSubject(phoneNumber)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(this.secretKey, SignatureAlgorithm.HS256)
                .compact();
    }


    public String extractPhoneNumber(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

/*    public boolean validateToken(String token) {
        try {
            return extractPhoneNumber(token) != null && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }*/

    public Boolean validateToken(String token, CustomCustomerService customCustomerService) {
        final String PhoneNumber = extractPhoneNumber(token);
        System.out.println(PhoneNumber + " PhoneNumber");
        try{

           CustomCustomer existingcustomer = customCustomerService.findCustomCustomerByPhone(PhoneNumber,Constant.COUNTRY_CODE);
            System.out.println(PhoneNumber + " PhoneNumber" + existingcustomer + " expired  " + isTokenExpired(token));
           if(existingcustomer!=null){
               return (PhoneNumber.equals(existingcustomer.getMobileNumber()) && !isTokenExpired(token));
           }
            return false;
        }catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }
}
