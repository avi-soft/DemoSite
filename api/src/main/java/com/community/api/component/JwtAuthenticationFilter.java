package com.community.api.component;

import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.services.CustomCustomerService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomCustomerService customCustomerService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            final String authorizationHeader = request.getHeader("Authorization");

            String phoneNumber = null;
            String jwt = null;

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                phoneNumber = jwtUtil.extractPhoneNumber(jwt);
            }

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

                jwt = authorizationHeader.substring(7);
                try {
                    phoneNumber = jwtUtil.extractPhoneNumber(jwt);

                } catch (IllegalArgumentException e) {
                    logger.info("Illegal Argument while fetching the number !!");
                    e.printStackTrace();
                } catch (ExpiredJwtException e) {
                    logger.info("Given jwt token is expired !!");
                    e.printStackTrace();
                } catch (MalformedJwtException e) {
                    logger.info("Some changed has done in token !! Invalid Token");
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else {
                logger.info("Invalid Header Value !! ");
            }

            if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                CustomCustomer customCustomer = customCustomerService.findCustomCustomerByPhone(phoneNumber,null);
               if(customCustomer!=null){
                   Boolean validateToken = this.jwtUtil.validateToken(jwt);
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
            chain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error authenticating request: " + e.getMessage());
        }
    }
}
