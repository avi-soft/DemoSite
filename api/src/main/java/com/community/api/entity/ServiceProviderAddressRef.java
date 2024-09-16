package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "custom_service_provider_address_ref")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ServiceProviderAddressRef {
    @Id
    @Column(name = "address_type_id")
    private int address_type_Id;
    @Column(name = "address_name")
    private String address_name;
}
