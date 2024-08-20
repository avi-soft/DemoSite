package com.community.api.dto;

import lombok.*;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Component
public class OTPRequest {
    private String otpEntered;
    private String mobileNumber;
    private String country_code;
    private String username;
}
