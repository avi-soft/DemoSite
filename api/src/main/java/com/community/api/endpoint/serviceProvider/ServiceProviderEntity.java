package com.community.api.endpoint.serviceProvider;



import com.community.api.entity.Privileges;
import com.community.api.entity.ResizedImage;
import com.community.api.entity.ServiceProviderAddress;
import com.community.api.entity.ServiceProviderInfra;
import com.community.api.entity.ServiceProviderLanguage;
import com.community.api.entity.ServiceProviderRank;
import com.community.api.entity.ServiceProviderTest;
import com.community.api.entity.ServiceProviderTestStatus;
import com.community.api.entity.Skill;
import com.community.api.utils.Document;
import com.community.api.utils.ServiceProviderDocument;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Date;
import java.util.List;

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

    private String date_of_birth;
    @Size(min = 12, max = 12)
    private String aadhaar_number;

    @Size(min = 10, max = 10)
    private String pan_number;
   /* @OneToOne(cascade = CascadeType.ALL)
    private Document personal_photo;*/
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
   /* @OneToOne(cascade = CascadeType.ALL)
    private Document business_photo;*/

    private Boolean isCFormAvailable;

    private String registration_number;

//    @Lob
//    @Basic(fetch = FetchType.LAZY)
//    @Column(name = "cFormPhoto", columnDefinition="BLOB")
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "c_form_photo_id")
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

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}) // Only persist/merge, no REMOVE
    @JoinColumn(name="test_status_id", referencedColumnName = "test_status_id")
    private ServiceProviderTestStatus testStatus;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}) // Only persist/merge, no REMOVE
    @JoinColumn(name="rank_id", referencedColumnName = "rank_id")
    private ServiceProviderRank ranking;

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

    @JsonIgnore
    @OneToMany(mappedBy = "service_provider", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    private List<ServiceProviderTest> serviceProviderTests;

    @JsonIgnore
    @OneToMany(mappedBy = "serviceProvider", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResizedImage> resizedImages;


    private String token;
    @Column
    private Integer totalSkillTestPoints;


    @OneToMany(mappedBy = "serviceProviderEntity", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<ServiceProviderDocument> documents;

}
