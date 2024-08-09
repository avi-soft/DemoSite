package com.community.api.utils;

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
public class DocumentType {
    @GeneratedValue
    @Id
    private long documentTypeId;
    private String description;
    @OneToOne
    @JoinColumn(name = "document_id")
    private Document document;
}
