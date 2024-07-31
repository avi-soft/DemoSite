package com.community.api.component;

import com.community.api.endpoint.customer.CustomerDTO;
import org.springframework.stereotype.Component;

@Component
public  class AuthResponse {
    private String token;
    private CustomerDTO userDetails;

    public AuthResponse(String token, CustomerDTO userDetails) {
        this.token = token;
        this.userDetails = userDetails;
    }

    public String getToken() {
        return token;
    }

    public CustomerDTO getUserDetails() {
        return userDetails;
    }
}