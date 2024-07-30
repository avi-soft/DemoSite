package com.community.api.component;

import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;

@Component
public class JwtUtil {


    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Value("${jwt.secret.key}")
    private String secretKeyString;

    private Key secretKey;


    @PostConstruct
    public void init() {

        byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(secretKeyString);
        this.secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
    }


    public String generateToken(String phoneNumber, String Role) {
        try {
            return Jwts.builder()
                    .setHeaderParam("typ", "JWT")
                    .claim("phoneNumber", phoneNumber)
                    .claim("role", Role)
                    .setSubject(phoneNumber)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Invalid JWT token", e);
        }
    }


    public String extractPhoneNumber(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
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


    public Boolean validateToken(String token, CustomCustomerService customCustomerService) {
        final String PhoneNumber = extractPhoneNumber(token);
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
