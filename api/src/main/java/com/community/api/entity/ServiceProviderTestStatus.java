package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "service_provider_test_status")
public class ServiceProviderTestStatus
{

        @Column(name = "test_status_id")
        @Id
        private Long test_status_id;
        @Column(name = "test_status_name")
        private String  test_status_name;
        @Column(name = "test_status_description")
        private String test_status_description;
        private String created_at,updated_at,created_by;
}

