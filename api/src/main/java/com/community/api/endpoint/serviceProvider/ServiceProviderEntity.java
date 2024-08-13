package com.community.api.endpoint.serviceProvider;

import com.community.api.endpoint.serviceProvider.enums.Equipment;
import com.community.api.endpoint.serviceProvider.enums.Skill;
import com.community.api.utils.Document;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
public class ServiceProviderEntity  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_ID;

    private String user_name;

    private String first_name;

    private String last_name;
    private String country_code;
    private String father_name;

    private Date date_of_birth;

    private String aadhaar_number;

    @Size(min = 10, max = 10)
    private String pan_number;
    private Document personal_photo;


   /* private String residentialAddress;


    private String state;


    private String district;


    private String city;

    @Size(min = 6, max = 6)
    private String pinCode;*/

    @Size(min = 9, max = 13)
    private String primary_mobile_number;
    private String otp;
    @Size(min = 9, max = 13)
    private String secondary_mobile_number;

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
//    @Column(name = "businessPhoto", columnDefinition="BLOB")
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "business_photo_id")
    private Document business_photo;

    private Boolean isCFormAvailable;

    private String registration_number;

//    @Lob
//    @Column(name = "cFormPhoto", columnDefinition="BLOB")
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "c_form_photo_id")
    private Document cFormPhoto;

    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Equipment> equipment;

    private Boolean has_technical_knowledge;

    @Min(0)
    private Integer work_experience_in_months;

    private String highest_qualification;

    @OneToMany
    private List<Skill> skills;

    @OneToOne(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "status_id", referencedColumnName = "statusId")
    private ServiceProviderStatus status;

}
