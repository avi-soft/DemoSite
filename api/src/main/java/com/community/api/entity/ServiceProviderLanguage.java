package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "custom_service_provider_language")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceProviderLanguage {
    @Id
    @Column(name = "language_id")
    private int language_id;
    @Column(name = "language_name")
    private String language_name;
}

