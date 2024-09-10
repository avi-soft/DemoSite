package com.community.api.dto;

import com.community.api.endpoint.customer.AddressDTO;
import com.community.api.entity.QualificationDetails;
import com.community.api.utils.Document;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.broadleafcommerce.common.audit.Auditable;
import org.broadleafcommerce.common.locale.domain.Locale;
import org.broadleafcommerce.profile.core.domain.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RetrieveCustomerDetailDto
{
    protected Long id;
    protected Auditable auditable;
    protected String username;
    protected String password;
    protected String emailAddress;
    protected String firstName;
    protected String lastName;
    protected String externalId;
    protected ChallengeQuestion challengeQuestion;
    protected String challengeAnswer;
    protected Locale customerLocale;
    protected Map<String, CustomerAttribute> customerAttributes ;
    protected List<CustomerPhone> customerPhones = new ArrayList();
    protected List<CustomerPayment> customerPayments = new ArrayList();
    protected String taxExemptionCode;
    protected String unencodedPassword;
    protected String unencodedChallengeAnswer;
    protected boolean anonymous;
    protected boolean cookied;
    protected boolean loggedIn;
    protected Map<String, Object> transientProperties = new HashMap();

    private String countryCode;
    private String mobileNumber;
    private String otp;
    private String fathersName;
    private String mothersName;
    private String dob;
    private String gender;
    private String adharNumber;
    private String category; //@TODO -make it int for using in cart
    private String subcategory;
    private String secondaryMobileNumber;
    private String whatsappNumber;
    private String secondaryEmail;
    List<QualificationDetails> qualificationDetailsList;
    private List<Document> documents;
    private List<AddressDTO> customerAddresses;

}
