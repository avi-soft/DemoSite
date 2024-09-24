package com.community.api.entity;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.utils.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;
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


    @Column(name = "category_issue_date")
    private String categoryValidUpto;

    @Column(name="religion")
    private String relgion;

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
    @OneToMany(mappedBy = "custom_customer", cascade = CascadeType.ALL, orphanRemoval = true)
    List<QualificationDetails> qualificationDetailsList;

    @Nullable
    @OneToMany(mappedBy = "custom_customer", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
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

    @Column(name = "residential_address")
    private String residentailAddress;

    @Column(name = "state")
    private String state;

    @Column(name = "district")
    private String district;

    @Column(name = "city")
    private String city;

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "disability_handicapped")
    private boolean disability=false;

    @Column(name = "is_ex_service_man")
    private boolean exService=false;

    @Column(name = "is_married")
    private boolean isMarried=false;

    @Column(name = "visible_identification_mark_1")
    private String identificationMark1;

    @Column(name = "visible_identification_mark_2")
    private String identificationMark2;

    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "customer_referrer",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "service_provider_id"))
    private ServiceProviderEntity ReferrerServiceProvider;


}
