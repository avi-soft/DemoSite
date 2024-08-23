package com.community.api.endpoint.serviceProvider;

//import com.community.api.endpoint.serviceProvider.enums.Equipment;
//import com.community.api.endpoint.serviceProvider.enums.Skill;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ehcache.impl.serialization.ByteArraySerializer;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "service_provider")
@Inheritance(strategy = InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor
//@Getter
//@Setter
public class ServiceProviderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userID;

    private String username;

    private String firstName;

    private String lastName;

    private String fatherName;

    private Date dateOfBirth;

    private String aadhaarNumber;

    @Size(min = 10, max = 10)
    private String panNumber;

//    @JsonSerialize(using = ByteArrayToBase64Serializer.class)
//    @JsonDeserialize(using = Base64ToByteArrayDeserializer.class)
//    @Lob
//    @Column(name = "personalPhoto", columnDefinition="BLOB")
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "personal_photo_id")
    private String personalPhoto;


    private String residentialAddress;


    private String state;


    private String district;


    private String city;

    @Size(min = 6, max = 6)
    private String pinCode;

    @NotBlank(message = "Primary Phone number should not be blank")
    @Size(min = 9, max = 13)
    private String primaryMobileNumber;

    @NotBlank
    @Size(min = 9, max = 13)
    private String secondaryMobileNumber;

    @NotBlank
    @Size(min = 9, max = 13)
    private String whatsappNumber;

    @Email
    private String primaryEmail;

    @Email
    private String secondaryEmail;

    private Boolean isRunningBusinessUnit;

    private String businessName;


    private String businessLocation;

    @Email
    private String businessEmail;

    private Integer numberOfEmployees;

//    @Lob
//    @Column(name = "businessPhoto", columnDefinition="BLOB")
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "business_photo_id")
    private String businessPhoto;

    private Boolean isCFormAvailable;

    private String registrationNumber;

//    @Lob
//    @Column(name = "cFormPhoto", columnDefinition="BLOB")
//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "c_form_photo_id")
    private String cFormPhoto;
//
//    @ElementCollection(targetClass = Equipment.class)
//    @CollectionTable(name = "service_provider_equipment", joinColumns = @JoinColumn(name = "user_id"))
//    @Enumerated(EnumType.STRING)
//    @Column(name = "equipment")
//    private Set<Equipment> equipment;

    private Boolean hasTechnicalKnowledge;

    @Min(0)
    private Integer workExperienceInMonths;

    private String highestQualification;

//    @ElementCollection(targetClass = Skill.class)
//    @CollectionTable(name = "service_provider_skills", joinColumns = @JoinColumn(name = "user_id"))
//    @Enumerated(EnumType.STRING)
//    @Column(name = "skill")
//    private Set<Skill> skills;

    public ServiceProviderStatus getStatus() {
        return status;
    }

    public void setStatus( ServiceProviderStatus status) {
        this.status = status;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "status_id", referencedColumnName = "statusId")
    private ServiceProviderStatus status;

//    private String otherSkill;

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public @Size(min = 10, max = 10) String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(@Size(min = 10, max = 10) String panNumber) {
        this.panNumber = panNumber;
    }

    public String getPersonalPhoto() {
        return personalPhoto;
    }

    public void setPersonalPhoto(String personalPhoto) {
        this.personalPhoto = personalPhoto;
    }

    public String getResidentialAddress() {
        return residentialAddress;
    }

    public void setResidentialAddress(String residentialAddress) {
        this.residentialAddress = residentialAddress;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public @Size(min = 6, max = 6) String getPinCode() {
        return pinCode;
    }

    public void setPinCode(@Size(min = 6, max = 6) String pinCode) {
        this.pinCode = pinCode;
    }

    public @NotBlank(message = "Primary Phone number should not be blank") @Size(min = 9, max = 13) String getPrimaryMobileNumber() {
        return primaryMobileNumber;
    }

    public void setPrimaryMobileNumber(@NotBlank(message = "Primary Phone number should not be blank") @Size(min = 9, max = 13) String primaryMobileNumber) {
        this.primaryMobileNumber = primaryMobileNumber;
    }

    public @NotBlank @Size(min = 9, max = 13) String getSecondaryMobileNumber() {
        return secondaryMobileNumber;
    }

    public void setSecondaryMobileNumber(@NotBlank @Size(min = 9, max = 13) String secondaryMobileNumber) {
        this.secondaryMobileNumber = secondaryMobileNumber;
    }

    public @NotBlank @Size(min = 9, max = 13) String getWhatsappNumber() {
        return whatsappNumber;
    }

    public void setWhatsappNumber(@NotBlank @Size(min = 9, max = 13) String whatsappNumber) {
        this.whatsappNumber = whatsappNumber;
    }

    public @Email String getPrimaryEmail() {
        return primaryEmail;
    }

    public void setPrimaryEmail(@Email String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public @Email String getSecondaryEmail() {
        return secondaryEmail;
    }

    public void setSecondaryEmail(@Email String secondaryEmail) {
        this.secondaryEmail = secondaryEmail;
    }

    public Boolean getRunningBusinessUnit() {
        return isRunningBusinessUnit;
    }

    public void setRunningBusinessUnit(Boolean runningBusinessUnit) {
        isRunningBusinessUnit = runningBusinessUnit;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessLocation() {
        return businessLocation;
    }

    public void setBusinessLocation(String businessLocation) {
        this.businessLocation = businessLocation;
    }

    public @Email String getBusinessEmail() {
        return businessEmail;
    }

    public void setBusinessEmail(@Email String businessEmail) {
        this.businessEmail = businessEmail;
    }

    public Integer getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void setNumberOfEmployees(Integer numberOfEmployees) {
        this.numberOfEmployees = numberOfEmployees;
    }

    public String getBusinessPhoto() {
        return businessPhoto;
    }

    public void setBusinessPhoto(String businessPhoto) {
        this.businessPhoto = businessPhoto;
    }

    public Boolean getCFormAvailable() {
        return isCFormAvailable;
    }

    public void setCFormAvailable(Boolean CFormAvailable) {
        isCFormAvailable = CFormAvailable;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getcFormPhoto() {
        return cFormPhoto;
    }

    public void setcFormPhoto(String cFormPhoto) {
        this.cFormPhoto = cFormPhoto;
    }

//    public Set<Equipment> getEquipment() {
//        return equipment;
//    }

//    public void setEquipment(Set<Equipment> equipment) {
//        this.equipment = equipment;
//    }

    public Boolean getHasTechnicalKnowledge() {
        return hasTechnicalKnowledge;
    }

    public void setHasTechnicalKnowledge(Boolean hasTechnicalKnowledge) {
        this.hasTechnicalKnowledge = hasTechnicalKnowledge;
    }

    public @Min(0) Integer getWorkExperienceInMonths() {
        return workExperienceInMonths;
    }

    public void setWorkExperienceInMonths(@Min(0) Integer workExperienceInMonths) {
        this.workExperienceInMonths = workExperienceInMonths;
    }

    public String getHighestQualification() {
        return highestQualification;
    }

    public void setHighestQualification(String highestQualification) {
        this.highestQualification = highestQualification;
    }

//    public Set<Skill> getSkills() {
//        return skills;
//    }

//    public void setSkills(Set<Skill> skills) {
//        this.skills = skills;
//    }
}
