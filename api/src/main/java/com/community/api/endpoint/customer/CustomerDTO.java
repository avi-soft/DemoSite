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

//    todo :- need to add role here
    private String username,password,oldPassword;
}
