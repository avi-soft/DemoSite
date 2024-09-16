package com.community.api.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerAddressDTO {
    private long addressId;
    private String addressName,addressLine1,city,state,pincode,district;
}
