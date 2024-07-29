package com.community.api.component;

import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.services.CustomCustomerService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.broadleafcommerce.profile.web.core.CustomerState;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenValidatorFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenValidatorFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CustomCustomerService customCustomerService;

    public JwtTokenValidatorFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

            if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
                logger.info("Invalid Header Value!");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
                return;
            }

            if(customCustomerService==null){
                logger.info("customCustomerService is null");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("JWT token is expired");
            }

            String jwt = authorizationHeader.substring(BEARER_PREFIX_LENGTH);
            String phoneNumber = jwtUtil.extractPhoneNumber(jwt);

            if (phoneNumber == null) {
                logger.info("Invalid JWT token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
                return;
            }

            CustomCustomer customCustomer = customCustomerService.findCustomCustomerByPhone(phoneNumber, null);

            if (customCustomer == null) {
                logger.info("Invalid data provided");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid data provided");
                return;
            }

            if (!jwtUtil.validateToken(jwt, customCustomerService)) {
                logger.info("Invalid JWT token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
                return;
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    phoneNumber, null, new ArrayList<>());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            /* CustomerState.setCustomer(customCustomer);
            this.setupCustomerForRuleProcessing(customCustomer, request);
            addCustomerToRuleMap(customCustomer, request);*/

            chain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            logger.info("Given jwt token is expired!");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("JWT token is expired");
        } catch (MalformedJwtException e) {
            logger.info("Some changes have been done in token! Invalid Token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid JWT token");
        } catch (Exception e) {
            logger.error("Internal Server Error", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Internal Server Error");
        }
    }

/*    private void addCustomerToRuleMap(CustomCustomer customCustomer, HttpServletRequest request) {
        Map<String, Object> ruleMap = (Map) request.getAttribute("blCustomRuleMap");

        if (ruleMap == null) {
            ruleMap = new HashMap<>();
        }

        ruleMap.put("customer", customCustomer);
        request.setAttribute("blCustomRuleMap", ruleMap);
    }*/
}