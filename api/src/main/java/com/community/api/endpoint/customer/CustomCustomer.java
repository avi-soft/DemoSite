package com.community.api.endpoint.customer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.broadleafcommerce.profile.core.domain.CustomerImpl;
import javax.persistence.*;

@Entity
@Table(name = "CUSTOM_CUSTOMER")
@Inheritance(strategy = InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomCustomer extends CustomerImpl
{
    @Column(name = "country_code", unique = false)
    private String countryCode;
    @Column(name = "mobile_number", unique = false)
    private String mobileNumber;
    @Column(name = "otp", unique = true)
    private String otp;

}
