package com.community.api.entity;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
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
    private String district,street,state,landmark,pincode;

    @ManyToOne(fetch = FetchType.LAZY) // Use lazy loading to improve performance if needed
    @JoinColumn(name = "service_provider_id") // Explicitly specify the foreign key column
    private ServiceProviderEntity serviceProviderEntity;
}
