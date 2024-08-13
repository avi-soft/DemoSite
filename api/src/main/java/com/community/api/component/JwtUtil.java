package com.community.api.component;

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
    private TokenBlacklist tokenBlacklist;

    @Autowired
    private CustomerService customerService;

    @PostConstruct
    public void init() {
        byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(secretKeyString);
        this.secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
    }

    public String generateToken(Long id, String role, String ipAddress, String userAgent) {
        try {
            String uniqueTokenId = UUID.randomUUID().toString();

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
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("id", Long.class);
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("JWT token is expired", e);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public Boolean validateToken(String token, String ipAddress, String userAgent) {


        try {
            Long id = extractId(token);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String tokenId = claims.getId();
            if (tokenBlacklist.isTokenBlacklisted(tokenId)) {
                return false;
            }

            Customer existingCustomer = customerService.readCustomerById(id);
            if (existingCustomer == null) {
                return false;
            }

            if (isTokenExpired(token)) {
                return false;
            }

            String storedIpAddress = claims.get("ipAddress", String.class);
            String storedUserAgent = claims.get("userAgent", String.class);

            return ipAddress.trim().equals(storedIpAddress != null ? storedIpAddress.trim() : "") &&
                    userAgent.trim().equalsIgnoreCase(storedUserAgent != null ? storedUserAgent.trim() : "");
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

    private boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            return expiration.before(new Date());
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error checking token expiration", e);
        }
    }

    public boolean logoutUser(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String uniqueTokenId = claims.getId();
            tokenBlacklist.blacklistToken(uniqueTokenId);
            return true;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return false;

        }
    }
}
