package com.community.api.endpoint.serviceProvider;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ServiceProviderStatus {

    @Id
    private Integer statusId;

    private String description;
    @OneToOne(mappedBy = "status")
    private ServiceProviderEntity serviceProviderEntity;
}
