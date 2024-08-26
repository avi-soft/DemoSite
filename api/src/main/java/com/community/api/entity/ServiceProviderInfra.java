package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "custom_service_provider_infra")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceProviderInfra {
        @Id
        @Column(name = "infra_id")
        private int infra_id;
        @Column(name = "infra_name")
        private String infra_name;
    }

