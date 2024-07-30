package com.community.api.component;

import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.services.CustomCustomerService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
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

import static org.ehcache.core.exceptions.StorePassThroughException.handleException;

@Component
public class JwtAuthenticationFilter  extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter .class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CustomCustomerService customCustomerService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {

            String requestURI = request.getRequestURI();
            if (requestURI.startsWith("/api/v1/account") || requestURI.startsWith("/api/v1/otp") || requestURI.startsWith("/api/v1/test")) {
                chain.doFilter(request, response);
                return;
            } else {
                boolean responseHandled = false;
                responseHandled = authenticateUser(request, response);
                if (!responseHandled) {
                    chain.doFilter(request, response);
                }
            }

        }catch (ExpiredJwtException e) {
            if (!response.isCommitted()) {
                handleException(response, HttpServletResponse.SC_BAD_REQUEST, "JWT token is expired");
            }
        } catch (MalformedJwtException e) {
            if (!response.isCommitted()) {
                handleException(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT token");
            }
        } catch (Exception e) {
            if (!response.isCommitted()) {
                handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
            }
        }
    }
    private void handleException(HttpServletResponse response, int statusCode, String message) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(statusCode);
            response.getWriter().write(message);
        }
    }

    private boolean authenticateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        logger.info(authorizationHeader + " authorizationHeader");

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("JWT token cannot be empty");
            }
            return true;
        }

        if (customCustomerService == null) {
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("customCustomerService is null");
            }
            return true;
        }

        String jwt = authorizationHeader.substring(BEARER_PREFIX_LENGTH);
        String phoneNumber = jwtUtil.extractPhoneNumber(jwt);

        if (phoneNumber == null) {
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid phoneNumber in token");
            }
            return true;
        }

        if (!jwtUtil.validateToken(jwt, customCustomerService)) {
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
            }
            return true;
        }

        if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            CustomCustomer customCustomer = customCustomerService.findCustomCustomerByPhone(phoneNumber, null);

            if (customCustomer != null) {
                Boolean validateToken = this.jwtUtil.validateToken(jwt, customCustomerService);

                if (validateToken) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            phoneNumber, null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    if (!response.isCommitted()) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("Invalid JWT token");
                    }
                    return true;
                }
            } else {
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid data provided");
                }
                return true;
            }
        }
        return false;
    }

    /*private void authenticateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final  String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        logger.info(authorizationHeader + " authorizationHeader");

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            logger.info("JWT token can not be empty!");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(" JWT token can not be empty");
            return;
        }

        if(customCustomerService==null){
            logger.info("customCustomerService is null");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("customCustomerService is null");
        }

        String jwt = authorizationHeader.substring(BEARER_PREFIX_LENGTH);
        String phoneNumber = jwtUtil.extractPhoneNumber(jwt);

        if (phoneNumber == null) {
            logger.info("Invalid phoneNumber in token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid phoneNumber in token");
            return;
        }



        if (!jwtUtil.validateToken(jwt, customCustomerService)) {
            logger.info("Invalid JWT token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid JWT token");
            return;
        }
        if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            CustomCustomer customCustomer = customCustomerService.findCustomCustomerByPhone(phoneNumber,null);

            if(customCustomer!=null){

                Boolean validateToken = this.jwtUtil.validateToken(jwt,customCustomerService);

                if (validateToken) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            phoneNumber, null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid JWT token");
                    return;
                }
            }else{
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid data provided");
                return;
            }
        }

    }*/

/*    private void addCustomerToRuleMap(CustomCustomer customCustomer, HttpServletRequest request) {
        Map<String, Object> ruleMap = (Map) request.getAttribute("blCustomRuleMap");

        if (ruleMap == null) {
            ruleMap = new HashMap<>();
        }

        ruleMap.put("customer", customCustomer);
        request.setAttribute("blCustomRuleMap", ruleMap);
    }*/
}