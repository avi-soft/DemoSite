package com.community.api.dto;

import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.entity.*;
import com.community.api.utils.Document;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RetrieveServiceProviderDetailDto
{
    private Long service_provider_id;
    private String user_name;
    private String first_name;
    private String last_name;
    private String country_code;
    private String father_name;
    private Date date_of_birth;
    private String aadhaar_number;
    private String pan_number;
    private Document personal_photo;
    private String mobileNumber;
    private String otp;
    private String secondary_mobile_number;
    private int role;
    private String whatsapp_number;
    private String primary_email;
    private String secondary_email;
    private String password;
    private Boolean is_running_business_unit;
    private String business_name;
    private String business_location;
    private String business_email;
    private Integer number_of_employees;
    private Document business_photo;
    private Boolean isCFormAvailable;
    private String registration_number;
    private Document cFormPhoto;
    private Boolean has_technical_knowledge;
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
    private List<Skill> skills;
    private List<ServiceProviderAddress> spAddresses;
    private ServiceProviderStatus status;
    private List<Privileges> privileges;
    private List<ServiceProviderInfra> infra;
    private List<ServiceProviderLanguage> languages;
}
