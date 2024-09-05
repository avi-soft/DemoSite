package com.community.api.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Table(name = "signature_image")
@Data
public class SignatureImage
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "signature_id")
    private Long signature_id;

    @Lob
    @Column(name = "signature_image_data", nullable = false)
    private byte[] signature_image_data;

    @Column(name = "signature_file_name")
    private String signature_file_name;

    @Column(name = "signature_content_type")
    private String signature_content_type;

}
