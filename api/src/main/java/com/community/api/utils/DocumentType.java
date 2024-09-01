package com.community.api.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "custom_document")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DocumentType {
    @Id
    @Column(name = "document_type_id")
    private Integer document_type_id;
    @Column(name = "document_type_name")
    private String document_type_name;
    @Column(name = "description")
    private String description;
}
