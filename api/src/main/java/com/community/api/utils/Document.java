package com.community.api.utils;

import com.community.api.entity.CustomCustomer;
import com.community.api.utils.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

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


    private String filePath;

    @Lob
    private byte[] data;

    @ManyToOne
    @JoinColumn(name = "custom_customer_id")
    private CustomCustomer customCustomer;

    @OneToOne
    @JoinColumn(name = "document_type_Id")
    private DocumentType documentType;


}
