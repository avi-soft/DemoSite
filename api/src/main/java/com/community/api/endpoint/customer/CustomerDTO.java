package com.community.api.endpoint.customer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Component
public class CustomerDTO {
    private String firstName,lastName,email,mobileNumber,username,role;

    private Long customerId;

}
