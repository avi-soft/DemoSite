package com.community.api.entity;

import com.community.api.utils.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micrometer.core.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.profile.core.domain.CustomerImpl;

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

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "mobile_number", unique = true)
    private String mobileNumber;

    @Column(name = "otp", unique = true)
    private String otp;


    @Column(name = "father_name")
    private String fathersName;

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

    @Column(name = "sub_category")
    private String subcategory;

    @OneToOne(cascade = CascadeType.ALL)
    private Document domicile;

    @Column(name = "secondary_mobile_number")
    private String secondaryMobileNumber;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;
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

    @OneToMany(mappedBy = "custom_customer", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    private List<Document> documents;

    @Nullable
    @ManyToMany
    @JoinTable(
            name = "cart_recovery_log", // The name of the join table
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
        private List<CustomProduct> cartRecoveryLog;
    private String token;
}
