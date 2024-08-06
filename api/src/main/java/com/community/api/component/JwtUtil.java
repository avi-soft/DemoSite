package com.community.api.component;

import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Value("${jwt.secret.key}")
    private String secretKeyString;

    private Key secretKey;

    @Autowired
    private TokenBlacklist tokenBlacklistService;

    @Autowired
    private CustomerService customerService;

    @PostConstruct
    public void init() {
        byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(secretKeyString);
        this.secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
    }

    public String generateToken(Long id, String role, String ipAddress, String userAgent) {
        try {
            String uniqueTokenId = UUID.randomUUID().toString() + ":" + id;

            return Jwts.builder()
                    .setHeaderParam("typ", "JWT")
                    .setId(uniqueTokenId)
                    .claim("id", id)
                    .claim("role", role)
                    .claim("ipAddress", ipAddress)
                    .claim("userAgent", userAgent)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))  // 10 hours expiration
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    public Long extractId(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
        try {
            return (long) Math.toIntExact(this.parseClaims(token)
                    .get("id", Long.class));
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("JWT token is expired", e);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public Boolean validateToken(String token, CustomCustomerService customCustomerService, String ipAddress, String userAgent) {
        Long id = extractId(token);

        try {

            Customer existingCustomer = customerService.readCustomerById(id);
            if (existingCustomer == null) {
                return false;
            }

            if (isTokenExpired(token)) {
                return false;
            }
            String uniqueTokenId = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getHeader()
                    .get("jti").toString();

            if (tokenBlacklistService.isTokenBlacklisted(uniqueTokenId)) {
                return false;
            }

            String trimmedIpAddress = ipAddress.trim();
            String trimmedUserAgent = userAgent.trim();

            String storedIpAddress = parseClaims(token).get("ipAddress", String.class);
            String storedUserAgent = parseClaims(token).get("userAgent", String.class);
            String trimmedStoredIpAddress = storedIpAddress != null ? storedIpAddress.trim() : "";
            String trimmedStoredUserAgent = storedUserAgent != null ? storedUserAgent.trim() : "";

            if (trimmedIpAddress.equals(trimmedStoredIpAddress) && trimmedUserAgent.equalsIgnoreCase(trimmedStoredUserAgent)) {
                return true;
            }

            return false;
        } catch (ExpiredJwtException e) {
            exceptionHandling.handleException(e);
            return false;
        } catch (MalformedJwtException | IllegalArgumentException e) {
            exceptionHandling.handleException(e);
            return false;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return false;
        }
    }


    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims parseClaimsHeaders(String token) {
        return (Claims) Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getHeader();
    }

    private boolean isTokenExpired(String token) {
        try {
            Date expiration = this.parseClaims(token)
                    .getExpiration();

            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error checking token expiration", e);
        }
    }
    public void logoutUser(String token) {
        String uniqueTokenId = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getHeader()
                .get("jti").toString();

        tokenBlacklistService.blacklistToken(uniqueTokenId);
    }

}
