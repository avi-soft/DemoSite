package com.community.api.endpoint.serviceProvider;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @Column(name = "status_id")
    @Id
    private Integer status_id;
    @Column(name = "status_name")
    private String status_name;
    @Column(name = "description")
    private String description;
    private String created_at,updated_at,created_by;
}
