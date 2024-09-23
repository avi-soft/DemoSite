package com.community.api.component;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.services.RoleService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private ExceptionHandlingImplement exceptionHandling;
    private RoleService roleService;
    private String secretKeyString = "DASYWgfhMLL0np41rKFAGminD1zb5DlwDzE1WwnP8es=";
    private Key secretKey;
    private EntityManager entityManager;
    private TokenBlacklist tokenBlacklist;
    private CustomerService customerService;

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setTokenBlacklist(TokenBlacklist tokenBlacklist) {
        this.tokenBlacklist = tokenBlacklist;
    }

    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostConstruct
    public void init() {
        try {
            byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(secretKeyString);
            System.out.println("Decoded key length (bytes): " + secretKeyBytes.length);
            if (secretKeyBytes.length * 8 < 256) {
                throw new IllegalArgumentException("Key length is less than 256 bits.");
            }
            this.secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

   /* @PostConstruct
    public void init() {
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }*/

    public String generateToken(Long id, Integer role, String ipAddress, String userAgent) {
        try {
            String uniqueTokenId = UUID.randomUUID().toString();

            return Jwts.builder()
                    .setHeaderParam("typ", "JWT")
                    .setId(uniqueTokenId)
                    .claim("id", id)
                    .claim("role", role)
                    .claim("ipAddress", ipAddress)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    private Key getSignInKey() {

        try {
            byte[] secretKeyBytes = DatatypeConverter.parseBase64Binary(secretKeyString);
            this.secretKey = Keys.hmacShaKeyFor(secretKeyBytes);
            return this.secretKey;
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error generating JWT token", e);
        }

    }

    public Long extractId(String token) {

        try {
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }

            if (isTokenExpired(token)) {
                throw new ExpiredJwtException(null, null, "Token is expired");
            }
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("id", Long.class);
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    @Transactional
    public Boolean validateToken(String token, String ipAddress, String userAgent) {

        try {

            if (isTokenExpired(token)) {
                throw new IllegalArgumentException("Token is expired");

            }

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
            int role=extractRoleId(token);
            Customer existingCustomer=null;
            ServiceProviderEntity existingServiceProvider=null;
            if(roleService.findRoleName(role).equals(Constant.roleUser)){
                existingCustomer = customerService.readCustomerById(id);
                if (existingCustomer == null) {
                    return false;
                }
            }
            else if(roleService.findRoleName(role).equals(Constant.roleServiceProvider)) {
                existingServiceProvider = entityManager.find(ServiceProviderEntity.class, id);
                if(existingServiceProvider==null)
                    return false;
            }

            String storedIpAddress = claims.get("ipAddress", String.class);


            return ipAddress.trim().equals(storedIpAddress != null ? storedIpAddress.trim() : "");
        } catch (ExpiredJwtException e) {
            logoutUser(token);
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
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token is required");

            }
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            return expiration.before(new Date());
        }catch (ExpiredJwtException e) {
            logoutUser(token);
            return false;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error checking token expiration", e);
        }
    }

    public boolean logoutUser(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {

                throw new IllegalArgumentException("Token is required");


            }
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String uniqueTokenId = claims.getId();
            tokenBlacklist.blacklistToken(token);
            return true;
        }catch (ExpiredJwtException e) {
            tokenBlacklist.blacklistToken(token);
            return true;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return false;
        }
    }

    public Integer extractRoleId(String token) {
        try {
            if (token == null || token.isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }
            if (isTokenExpired(token)) {

                throw new ExpiredJwtException(null, null, "Token is expired");

            }
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("role", Integer.class);

        } catch (SignatureException e) {
            throw new RuntimeException("Invalid JWT signature.");
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            throw new RuntimeException("Error in JWT token", e);
        }
    }
}
