package com.community.api.entity;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "service_provider_test")
@NoArgsConstructor
@Data
public class ServiceProviderTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Long testId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_provider_id", nullable = false)
    private ServiceProviderEntity serviceProvider;

    @Column(name = "downloaded_image_url")
    private String downloadedImageUrl;
    @Column(name = "resized_image_url")
    private String resizedImageUrl;
    @Column(name = "signature_url")
    private String signatureImageUrl;
    @Column(name = "typing_test_text")
    private String typingTestText;
    @Column(name = "is_typing_test_passed")
    private Boolean isTypingTestPassed;
}
