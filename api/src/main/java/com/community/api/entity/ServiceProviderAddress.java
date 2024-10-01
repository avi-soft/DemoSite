package com.community.api.entity;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "custom_service_provider_address")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceProviderAddress
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate the ID
    @Column(name = "address_Id")
    private long address_id;
    private int address_type_id;
    private String district,address_line,state,/*city*/pincode;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY) // Use lazy loading to improve performance if needed
    @JoinColumn(name = "service_provider_id") // Explicitly specify the foreign key column
    @JsonIgnore
    private ServiceProviderEntity serviceProviderEntity;
}
