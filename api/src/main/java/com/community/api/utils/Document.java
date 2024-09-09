package com.community.api.utils;

import com.community.api.entity.CustomCustomer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long document_id;

    private String name;

    @Column(name = "file_path")
    private String file_path;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "data")
    private byte[] data;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "custom_customer_id")
    @JsonIgnore
    private CustomCustomer custom_customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_type_id")
    private DocumentType document_type;

}