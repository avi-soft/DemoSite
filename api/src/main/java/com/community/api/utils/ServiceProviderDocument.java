package com.community.api.utils;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceProviderDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    private String name;
    private String filePath;

    @Lob
    private byte[] data;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "service_provider_id")
    private ServiceProviderEntity serviceProviderEntity;


    @ManyToOne
    @JoinColumn(name = "document_type_Id")
    private DocumentType documentType;


}
