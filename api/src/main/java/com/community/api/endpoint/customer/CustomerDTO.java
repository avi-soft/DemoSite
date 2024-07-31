package com.community.api.endpoint.customer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CustomerDTO {
    private String firstName,lastName,email,mobileNumber,username,role;

    private Long customerId;

}
