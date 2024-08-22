package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "service_provider_address_ref")
@NoArgsConstructor
@Getter
@Setter
public class ServiceProviderAddressRef {
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    private int address_type_Id;
    private String address_name;
}
