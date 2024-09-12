package com.community.api.entity;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class SignatureImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "image_data")
    @JsonIgnore
    private byte[] image_data;

    @Column(name = "file_name")
    private String file_name;

    @Column(name = "file_type")
    private String file_type;

    @Column(name="file_path")
    private String file_path;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_provider_id")
    private ServiceProviderEntity serviceProvider;
}

