package com.community.api.entity;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.utils.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.micrometer.core.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.profile.core.domain.CustomerImpl;
import org.springframework.ldap.odm.annotations.Attribute;

import javax.lang.model.element.Name;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "CUSTOM_CUSTOMER")
@Inheritance(strategy = InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomCustomer extends CustomerImpl {

    @Nullable
    @Column(name = "country_code")
    private String countryCode;

    @Nullable
    @Column(name = "mobile_number", unique = true)
    private String mobileNumber;

    @Nullable
    @Column(name = "otp", unique = true)
    private String otp;


    @Column(name = "father_name")
    private String fathersName;

    @Nullable
    @Column(name = "pan_number")
    private String panNumber;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "mother_name")
    private String mothersName;

    @Column(name = "date_of_birth")
    private String dob;

    @Column(name = "gender")
    private String gender;

    @Column(name = "adhar_number", unique = true)
    @Size(min = 12, max = 12)
    private String adharNumber;

    @Column(name = "category")
    private String category; //@TODO -make it int for using in cart


    @Column(name = "category_issue_date", insertable = false, updatable = false)
    private String categoryIssueDate;

    @Column(name = "height_cms")
    private String heightCms;

    @Column(name = "weight_kgs")
    private String weightKgs;

    @Column(name = "chest_size_cms")
    private String chestSizeCms;

    @Column(name = "shoe_size_inches")
    private String shoeSizeInches;

    @Column(name = "waist_size_cms")
    private String waistSizeCms;

    @Column(name = "can_swim")
    private Boolean canSwim; // Yes/No

    @Column(name = "proficiency_in_sports_national_level")
    private Boolean proficiencyInSportsNationalLevel; // Yes/No

    @Column(name = "first_choice_exam_city")
    private String firstChoiceExamCity;

    @Column(name = "second_choice_exam_city")
    private String secondChoiceExamCity;

    @Column(name = "third_choice_exam_city")
    private String thirdChoiceExamCity;

    @Column(name = "mphil_passed")
    private Boolean mphilPassed;

    @Column(name = "phd_passed")
    private Boolean phdPassed;

    @Column(name = "number_of_attempts")
    private Integer numberOfAttempts;


    @Column(name = "work_experience")
    private String workExperience; // State level/Centre level, Govt./Private

    @Column(name = "category_issue_date")
    private String categoryValidUpto;

    @Column(name="religion")
    private String religion;

    @Column(name = "belongs_to_minority")
    private Boolean belongsToMinority=false;

    @Nullable
    @Column(name = "sub_category")
    private String subcategory;

    @Nullable
    @Column(name = "domicile")
    private Boolean domicile=false;

    @Nullable
    @Column(name = "secondary_mobile_number")
    private String secondaryMobileNumber;

    @Column(name = "show_contact_details")
    private Boolean show_my_contact_details=false;

    @Nullable
    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    @Nullable
    @Column(name = "secondary_email")
    private String secondaryEmail;

    @Nullable
    @ManyToMany
    @JoinTable(
            name = "customer_saved_forms",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private List<CustomProduct>savedForms;

    @Nullable
    @JsonManagedReference("qualificationDetailsList-customer")
    @OneToMany(mappedBy = "custom_customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QualificationDetails> qualificationDetailsList;

    @Nullable
    @JsonManagedReference("documents-customer")
    @OneToMany(mappedBy = "custom_customer", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    private List<Document> documents;

    @Nullable
    @ManyToMany
    @JoinTable(
            name = "cart_recovery_log", // The name of the join table
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
        private List<CustomProduct> cartRecoveryLog;

    @Nullable
    private String token;



    @Column(name = "disability_handicapped")
    private Boolean disability=false;

    @Column(name = "is_ex_service_man")
    private Boolean exService=false;

    @Column(name = "is_married")
    private Boolean isMarried=false;

    @Column(name = "visible_identification_mark_1")
    private String identificationMark1;

    @Column(name = "visible_identification_mark_2")
    private String identificationMark2;

    @Nullable
//    @JsonManagedReference("referrerServiceProvider-customer")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "customer_referrer",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "service_provider_id"))
    private ServiceProviderEntity ReferrerServiceProvider;


}
