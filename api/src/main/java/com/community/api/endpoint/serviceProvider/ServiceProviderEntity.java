package com.community.api.endpoint.serviceProvider;


import com.community.api.entity.*;
import com.community.api.utils.Document;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.micrometer.core.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.broadleafcommerce.profile.core.domain.Address;
import org.ehcache.impl.serialization.ByteArraySerializer;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "service_provider")
@Inheritance(strategy = InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ServiceProviderEntity  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long service_provider_id;

    private String user_name;

    private String first_name;

    private String last_name;
    //@TODO-countryCode to country_code for both customer and service provider
    private String country_code;
    private String father_name;

    private Date date_of_birth;

    private String aadhaar_number;

    @Size(min = 10, max = 10)
    private String pan_number;
    @OneToOne(cascade = CascadeType.ALL)
    private Document personal_photo;
    @Size(min = 9, max = 13)
    private String mobileNumber;
    private String otp;
    @Size(min = 9, max = 13)
    private String secondary_mobile_number;
    private int role;
    @Size(min = 9, max = 13)
    private String whatsapp_number;
    @Email
    private String primary_email;

    @Email
    private String secondary_email;
    private String password;
    private Boolean is_running_business_unit;

    private String business_name;

    private String business_location;

    @Email
    private String business_email;

    private Integer number_of_employees;

//    @Lob
//    @Basic(fetch = FetchType.LAZY)
//    @Column(name = "businessPhoto", columnDefinition="BLOB")
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "business_photo_id")
    @OneToOne(cascade = CascadeType.ALL)
    private Document business_photo;

    private Boolean isCFormAvailable;

    private String registration_number;

//    @Lob
//    @Basic(fetch = FetchType.LAZY)
//    @Column(name = "cFormPhoto", columnDefinition="BLOB")
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "c_form_photo_id")

    @OneToOne(cascade = CascadeType.ALL)
    private Document cFormPhoto;

   /*@OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Equipment> equipment;*/

    private Boolean has_technical_knowledge;

    @Min(0)
    private Integer work_experience_in_months;

    private String highest_qualification;
    private String name_of_institute;
    private String year_of_passing;
    private String board_or_university;
    private String total_marks;
    private String marks_obtained;
    private String cgpa;
    private double latitude,longitude;
    private int rank;
    private int signedUp=0;

    @ManyToMany
    @JoinTable(
            name = "service_provider_skill", // The name of the join table
            joinColumns = @JoinColumn(name = "service_provider_id"), // Foreign key for ServiceProvider
            inverseJoinColumns = @JoinColumn(name = "skill_id")) // Foreign key for Skill
    private List<Skill> skills;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "service_provider_id")
    private List<ServiceProviderAddress> spAddresses;

    @OneToOne(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "status_id", referencedColumnName = "status_id")
    private ServiceProviderStatus status;

    @ManyToMany
    @JoinTable(
            name = "service_provider_privileges", // The name of the join table
            joinColumns = @JoinColumn(name = "service_provider_id"), // Foreign key for ServiceProvider
            inverseJoinColumns = @JoinColumn(name = "privilege_id")) // Foreign key for Privilege
    private List<Privileges> privileges;
    @ManyToMany
    @JoinTable(
            name = "service_provider_infra", // The name of the join table
            joinColumns = @JoinColumn(name = "service_provider_id"), // Foreign key for ServiceProvider
            inverseJoinColumns = @JoinColumn(name = "infra_id")) // Foreign key for Skill
    private List<ServiceProviderInfra> infra;
    @ManyToMany
    @JoinTable(
            name = "service_provider_languages", // The name of the join table
            joinColumns = @JoinColumn(name = "service_provider_id"), // Foreign key for ServiceProvider
            inverseJoinColumns = @JoinColumn(name = "language_id")) // Foreign key for Skill
    private List<ServiceProviderLanguage> languages;

    @OneToMany(mappedBy = "service_provider", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    private List<ServiceProviderTest> serviceProviderTests;

    @OneToMany(mappedBy = "serviceProvider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResizedImage> resizedImages;
}
