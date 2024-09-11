package com.community.api.component;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.ServiceProviderInfra;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.community.api.services.RoleService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.apache.solr.client.solrj.io.stream.SolrStream;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.persistence.EntityManager;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();
    private String apiKey="IaJGL98yHnKjnlhKshiWiy1IhZ+uFsKnktaqFX3Dvfg=\n";

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CustomCustomerService customCustomerService;
    @Autowired
    private RoleService roleService;

    @Autowired
    private CustomerService CustomerService;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    private EntityManager entityManager;
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
            if (isApiKeyRequiredUri(request) && validateApiKey(request)) {
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
        exceptionHandling.handleException(e);
        logger.error("ExpiredJwtException caught: {}", e.getMessage());
    } catch (MalformedJwtException e) {
        handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid JWT token");
        exceptionHandling.handleException(e);
        logger.error("MalformedJwtException caught: {}", e.getMessage());
    } catch (Exception e) {
        handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        exceptionHandling.handleException(e);

        logger.error("Exception caught: {}", e.getMessage());
    }

    }

    private boolean isApiKeyRequiredUri(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String path = requestURI.split("\\?")[0].trim();

        List<String> bypassUris = Arrays.asList(
                "/api/v1/categoryCustom/getProductsByCategoryId",
                "/api/v1/categoryCustom/getAllCategories"
        );

        boolean isBypassed = bypassUris.stream().anyMatch(path::equals);
        logger.info(isBypassed + " isBypassed");
        return isBypassed;
    }

    private boolean validateApiKey(HttpServletRequest request) {
        String requestApiKey = request.getHeader("x-api-key");
        logger.info(apiKey + " apiKey", requestApiKey + "requestApiKey");
        return apiKey.equals(requestApiKey);
    }

    private boolean isUnsecuredUri(String requestURI) {
        return requestURI.startsWith("/api/v1/account")
                || requestURI.startsWith("/api/v1/otp")
                || requestURI.startsWith("/api/v1/test")
                || requestURI.startsWith("/api/v1/swagger-ui.html")
                || requestURI.startsWith("/api/v1/swagger-resources")
                || requestURI.startsWith("/api/v1/v2/api-docs")
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
        String ipAdress = request.getRemoteAddr();
        String User_Agent = request.getHeader("User-Agent");

        if (!jwtUtil.validateToken(jwt, ipAdress, User_Agent)) {
            respondWithUnauthorized(response, "Invalid JWT token");
            return true;
        }
        Customer customCustomer = null;
        ServiceProviderEntity serviceProvider = null;
        if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (roleService.findRoleName(jwtUtil.extractRoleId(jwt)).equals(Constant.roleUser)) {
                customCustomer = CustomerService.readCustomerById(id);
                if (customCustomer != null && jwtUtil.validateToken(jwt, ipAdress, User_Agent)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            customCustomer.getId(), null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    return false;
                } else {
                    respondWithUnauthorized(response, "Invalid data provided for this customer");
                    return true;
                }
            } else if (roleService.findRoleName(jwtUtil.extractRoleId(jwt)).equals(Constant.roleServiceProvider)) {
              serviceProvider=entityManager.find(ServiceProviderEntity.class,id);
                if (serviceProvider != null && jwtUtil.validateToken(jwt, ipAdress, User_Agent)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            serviceProvider.getService_provider_id(), null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    return false;
                } else {
                    respondWithUnauthorized(response, "Invalid data provided for this customer");
                    return true;
                }
            }
        }
        return false;
    }

    private void respondWithUnauthorized(HttpServletResponse response, String message) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":401,\"message\":\"" + message + "\"}");


            response.getWriter().flush();
        }
    }

    private void handleException(HttpServletResponse response, int statusCode, String message) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(statusCode);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":" + statusCode + ",\"message\":\"" + message + "\"}");
            response.getWriter().flush();
        }
    }
}
