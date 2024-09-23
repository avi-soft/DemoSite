package com.community.api.entity;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_provider_test")
@NoArgsConstructor
@Data
public class ServiceProviderTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Long test_id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_provider_id", nullable = false)
    private ServiceProviderEntity service_provider;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "downloaded_image_id", nullable = true)
    private Image downloaded_image;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "resized_image_data")
    @JsonIgnore
    private byte[] resized_image_data;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resized_image_id", nullable = true)
    private ResizedImage resized_image;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "signature_image_id", nullable = true)
    private SignatureImage signature_image;

    @Column(name = "typing_test_text", columnDefinition = "TEXT")
    private String typing_test_text;

    @Column(name = "submitted_text", columnDefinition = "TEXT")
    private String submitted_text;

    @Column(name = "is_typing_test_passed")
    private Boolean is_typing_test_passed;

    @Column(name ="is_image_test_passed")
    private Boolean is_image_test_passed;

    @Column(name ="is_signature_test_passed")
    private Boolean is_test_completed;

    @Column(name = "submitted_at")
    private LocalDateTime submitted_at;

}
