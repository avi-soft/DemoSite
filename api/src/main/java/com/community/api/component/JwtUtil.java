package com.community.api.component;

import com.community.api.endpoint.customer.CustomCustomer;
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

    private Key secretKey;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private CustomerService customerService;
    @PostConstruct
    public void init() {
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public String generateToken(String phoneNumber) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(phoneNumber)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(secretKey, SignatureAlgorithm.HS256)
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

    public boolean validateToken(String token) {
        try {
            return extractPhoneNumber(token) != null && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

   /* public Boolean validateToken(String token, CustomCustomer customCustomerService) {
        final String PhoneNumber = extractPhoneNumber(token);
        try{
            return (PhoneNumber.equals(customCustomerService.findCustomCustomerByPhone(PhoneNumber,null)) && !isTokenExpired(token));
        }catch (Exception e) {
            return false;
        }
    }*/

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
