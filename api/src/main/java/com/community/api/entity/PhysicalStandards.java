package com.community.api.entity;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "physical_standards")
@Data
public class PhysicalStandards {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Assuming there's an ID field

    @Column(name = "height_cms")
    private Double heightCms;

    @Column(name = "weight_kgs")
    private Double weightKgs;

    @Column(name = "chest_size_cms")
    private Double chestSizeCms;

    @Column(name = "shoe_size_inches")
    private Double shoeSizeInches;

    @Column(name = "waist_size_cms")
    private Double waistSizeCms;

    @Column(name = "can_swim")
    private Boolean canSwim; // Yes/No

    @Column(name = "proficiency_in_sports_national_level")
    private Boolean proficiencyInSportsNationalLevel; // Yes/No

    @Column(name = "ncc_certificate")
    private String nccCertificate; // A/B/C

    @Column(name = "nss_certificate")
    private String nssCertificate;

    @Column(name = "sports_certificate")
    private String sportsCertificate; // State/Center level

    @Column(name = "work_experience")
    private String workExperience; // State level/Centre level, Govt./Private
}