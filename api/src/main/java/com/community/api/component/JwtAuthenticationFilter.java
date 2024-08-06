package com.community.api.component;
import com.community.api.services.CustomCustomerService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CustomCustomerService customCustomerService;

    @Autowired
    private CustomerService CustomerService;
    @Autowired
    private TokenBlacklist tokenBlacklistService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        try {

            String requestURI = request.getRequestURI();
            if (isUnsecuredUri(requestURI)) {
                chain.doFilter(request, response);
                return;
            }

            boolean responseHandled = authenticateUser(request, response);
            if (!responseHandled) {
                chain.doFilter(request, response);
            }else{
                return;
            }

        } catch (ExpiredJwtException e) {
            handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "JWT token is expired");
            logger.error("ExpiredJwtException caught: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid JWT token");
            logger.error("MalformedJwtException caught: {}", e.getMessage());
        } catch (Exception e) {
            handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            logger.error("Exception caught: {}", e.getMessage());
        }
    }



    private boolean isUnsecuredUri(String requestURI) {
        return requestURI.startsWith("/api/v1/account")
                || requestURI.startsWith("/api/v1/otp")
                || requestURI.startsWith("/api/v1/test")
                || requestURI.startsWith("/api/v1/swagger-ui.html")
                || requestURI.startsWith("/v3/api-docs")
                || requestURI.startsWith("/api/v1/images")
                 || requestURI.startsWith("/api/v1/webjars");
    }


    private boolean authenticateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            respondWithUnauthorized(response, "JWT token cannot be empty");
            return true;
        }

        if (customCustomerService == null) {
            respondWithUnauthorized(response, "CustomCustomerService is null");
            return true;
        }

        String jwt = authorizationHeader.substring(BEARER_PREFIX_LENGTH);
        Long id = jwtUtil.extractId(jwt);

        if (id == null) {
            respondWithUnauthorized(response, "Invalid details in token");
            return true;
        }
        String  ipAdress =request.getRemoteAddr();
        String User_Agent =   request.getHeader("User-Agent");

        System.out.println(ipAdress + " ipAdress" + User_Agent + " User_Agent authenticateUser" );
        if (!jwtUtil.validateToken(jwt, customCustomerService, ipAdress, User_Agent)) {
            respondWithUnauthorized(response, "Invalid JWT token");
            return true;
        }

        if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Customer customCustomer = CustomerService.readCustomerById(id);
            if (customCustomer != null && jwtUtil.validateToken(jwt, customCustomerService, ipAdress, User_Agent)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        customCustomer.getId(), null, new ArrayList<>());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                return false;
            } else {
                respondWithUnauthorized(response, "Invalid data provided for this customer");
                return true;
            }
        }
        return false;
    }

    private void respondWithUnauthorized(HttpServletResponse response, String message) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(message);
        }
    }

    private void handleException(HttpServletResponse response, int statusCode, String message) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(statusCode);
            response.getWriter().write(message);
        }
    }
}
