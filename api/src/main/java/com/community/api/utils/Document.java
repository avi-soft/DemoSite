package com.community.api.utils;
import com.community.api.entity.CustomCustomer;
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
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    private String name;
    private String filePath;

    @Lob
    private byte[] data;

    @JsonBackReference("documents-customer")
    @ManyToOne
    @JoinColumn(name = "custom_customer_id")
    private CustomCustomer custom_customer;

    @ManyToOne
    @JoinColumn(name = "document_type_Id")
    private DocumentType documentType;
}