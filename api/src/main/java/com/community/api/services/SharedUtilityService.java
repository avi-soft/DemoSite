package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.endpoint.customer.AddressDTO;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.*;
import com.community.api.utils.DocumentType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.SanitizableData;
import org.springframework.boot.actuate.endpoint.Sanitizer;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SharedUtilityService {
    private EntityManager entityManager;
    private ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setProductReserveCategoryFeePostRefService(ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService) {
        this.productReserveCategoryFeePostRefService = productReserveCategoryFeePostRefService;
    }

    public long findCount(String queryString) {
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult();
    }
    public static String getCurrentTimestamp() {
        // Get the current date and time with timezone
        ZonedDateTime zonedDateTime = ZonedDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSXXX");
        return zonedDateTime.format(formatter);
    }
    public Map<String,Object> createProductResponseMap(Product product, OrderItem orderItem)
    {
        Map<String, Object> productDetails = new HashMap<>();
        CustomProduct customProduct=entityManager.find(CustomProduct.class,product.getId());
        if(orderItem!=null)
            productDetails.put("order_item_id",orderItem.getId());
        productDetails.put("product_id", product.getId());
        productDetails.put("url", product.getUrl());
        productDetails.put("url_key", product.getUrlKey());
        productDetails.put("platform_fee",customProduct.getPlatformFee());
        productDetails.put("display_template", product.getDisplayTemplate());
        productDetails.put("default_sku_id", product.getDefaultSku().getId());
        productDetails.put("default_sku_name", product.getDefaultSku().getName());
        productDetails.put("sku_description", product.getDefaultSku().getDescription());
        productDetails.put("long_description", product.getDefaultSku().getLongDescription());
        productDetails.put("active_start_date", product.getDefaultSku().getActiveStartDate());//@TODO-Fee is dependent on category
        productDetails.put("fee",productReserveCategoryFeePostRefService.getCustomProductReserveCategoryFeePostRefByProductIdAndReserveCategoryId(product.getId(),1L).getFee());//this is dummy data
        productDetails.put("category_id",product.getDefaultCategory().getId());
        productDetails.put("active_end_date", product.getDefaultSku().getActiveEndDate());
        return productDetails;
    }
    public enum ValidationResult {
        SUCCESS,
        EXCEEDS_MAX_SIZE,
        EXCEEDS_NESTED_SIZE,
        INVALID_TYPE
    }


    public Map<String,Object> breakReferenceForCustomer(Customer customer)
    {
        Map<String,Object>customerDetails=new HashMap<>();
        customerDetails.put("id", customer.getId());
        customerDetails.put("dateCreated", customer.getAuditable().getDateCreated());
        customerDetails.put("createdBy", customer.getAuditable().getCreatedBy());
        customerDetails.put("dateUpdated", customer.getAuditable().getDateUpdated());
        customerDetails.put("updatedBy", customer.getAuditable().getUpdatedBy());
        customerDetails.put("username", customer.getUsername());
        customerDetails.put("password", customer.getPassword());
        customerDetails.put("emailAddress", customer.getEmailAddress());
        customerDetails.put("firstName", customer.getFirstName());
        customerDetails.put("lastName", customer.getLastName());
        customerDetails.put("externalId", customer.getExternalId());
        customerDetails.put("challengeQuestion", customer.getChallengeQuestion());
        customerDetails.put("challengeAnswer", customer.getChallengeAnswer());
        customerDetails.put("passwordChangeRequired", customer.isPasswordChangeRequired());
        customerDetails.put("receiveEmail", customer.isReceiveEmail());
        customerDetails.put("registered", customer.isRegistered());
        customerDetails.put("deactivated", customer.isDeactivated());
        customerDetails.put("customerPayments", customer.getCustomerPayments());
        customerDetails.put("taxExemptionCode", customer.getTaxExemptionCode());
        customerDetails.put("unencodedPassword", customer.getUnencodedPassword());
        customerDetails.put("unencodedChallengeAnswer", customer.getUnencodedChallengeAnswer());
        customerDetails.put("anonymous", customer.isAnonymous());
        customerDetails.put("cookied", customer.isCookied());
        customerDetails.put("loggedIn", customer.isLoggedIn());
        customerDetails.put("transientProperties", customer.getTransientProperties());
        CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customer.getId());
        customerDetails.put("countryCode", customCustomer.getCountryCode());
        customerDetails.put("mobileNumber", customCustomer.getMobileNumber());
        customerDetails.put("otp", customCustomer.getOtp());
        customerDetails.put("fathersName", customCustomer.getFathersName());
        customerDetails.put("mothersName", customCustomer.getMothersName());
        customerDetails.put("panNumber",customCustomer.getPanNumber());
        customerDetails.put("nationality",customCustomer.getNationality());
        customerDetails.put("dob", customCustomer.getDob());
        customerDetails.put("gender", customCustomer.getGender());
        customerDetails.put("adharNumber", customCustomer.getAdharNumber());
        customerDetails.put("category", customCustomer.getCategory());
        customerDetails.put("subcategory", customCustomer.getSubcategory());
        customerDetails.put("domicile", customCustomer.getDomicile());
        customerDetails.put("secondaryMobileNumber", customCustomer.getSecondaryMobileNumber());
        customerDetails.put("whatsappNumber", customCustomer.getWhatsappNumber());
        customerDetails.put("secondaryEmail", customCustomer.getSecondaryEmail());
        customerDetails.put("state", customCustomer.getState());
        customerDetails.put("city", customCustomer.getCity());
        customerDetails.put("district", customCustomer.getDistrict());
        customerDetails.put("pincode", customCustomer.getPincode());
        customerDetails.put("residentialAddress",customCustomer.getResidentailAddress());
      /*  customerDetails.put("qualificationDetails",customCustomer.getQualificationDetailsList());
        customerDetails.put("documentList",customCustomer.getDocumentList());
        List<Map<String,Object>>listOfSavedProducts=new ArrayList<>();*/
    /*    if(!customCustomer.getSavedForms().isEmpty()) {
            for (Product product : customCustomer.getSavedForms()) {
                listOfSavedProducts.add(createProductResponseMap(product, null));
            }
        }

        customerDetails.put("savedForms",listOfSavedProducts);*/
            List<CustomerAddressDTO>addresses=new ArrayList<>();
        for(CustomerAddress customerAddress:customer.getCustomerAddresses())
        {
            CustomerAddressDTO addressDTO=new CustomerAddressDTO();
            addressDTO.setAddressId(customerAddress.getId());
            addressDTO.setAddressName(customerAddress.getAddressName());
            addressDTO.setAddressLine1(customerAddress.getAddress().getAddressLine1());
            addressDTO.setState(customerAddress.getAddress().getStateProvinceRegion());
            addressDTO.setPincode(customerAddress.getAddress().getPostalCode());
            addressDTO.setDistrict(customerAddress.getAddress().getCounty());
            addressDTO.setCity(customerAddress.getAddress().getCity());
            addresses.add(addressDTO);
        }
        customerDetails.put("addresses",addresses);

        return customerDetails;
    }
    public ValidationResult validateInputMap(Map<String,Object>inputMap)
    {
            if(inputMap.keySet().size()>Constant.MAX_REQUEST_SIZE)
                return ValidationResult.EXCEEDS_MAX_SIZE;

            // Iterate through the map entries to check for nested maps
            for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
                Object value = entry.getValue();

                // Check if the value is a nested map
                if (value instanceof Map) {
                    Map<?, ?> nestedMap = (Map<?, ?>) value;

                    // Check the size of the nested map's key set
                    if (nestedMap.keySet().size() > Constant.MAX_NESTED_KEY_SIZE) {
                        return ValidationResult.EXCEEDS_NESTED_SIZE;
                    }
                }
            }
            return ValidationResult.SUCCESS;

        }

    public Map<String,Object> serviceProviderDetailsMap(ServiceProviderEntity serviceProvider)
    {
        Map<String,Object>serviceProviderDetails=new HashMap<>();
        serviceProviderDetails.put("id", serviceProvider.getService_provider_id());
        serviceProviderDetails.put("user_name", serviceProvider.getUser_name());
        serviceProviderDetails.put("first_name", serviceProvider.getFirst_name());
        serviceProviderDetails.put("last_name", serviceProvider.getLast_name());
        serviceProviderDetails.put("country_code", serviceProvider.getCountry_code());
        serviceProviderDetails.put("father_name", serviceProvider.getFather_name());
        serviceProviderDetails.put("date_of_birth", serviceProvider.getDate_of_birth());
        serviceProviderDetails.put("aadhaar_number", serviceProvider.getAadhaar_number());
        serviceProviderDetails.put("pan_number", serviceProvider.getPan_number());
        serviceProviderDetails.put("mobileNumber", serviceProvider.getMobileNumber());
        serviceProviderDetails.put("secondary_mobile_number", serviceProvider.getSecondary_mobile_number());
        serviceProviderDetails.put("role", serviceProvider.getRole());
        serviceProviderDetails.put("whatsapp_number", serviceProvider.getWhatsapp_number());
        serviceProviderDetails.put("primary_email", serviceProvider.getPrimary_email());
        serviceProviderDetails.put("secondary_email", serviceProvider.getSecondary_email());
        serviceProviderDetails.put("password", serviceProvider.getPassword());
        serviceProviderDetails.put("is_running_business_unit", serviceProvider.getIs_running_business_unit());
        serviceProviderDetails.put("business_name", serviceProvider.getBusiness_name());
        serviceProviderDetails.put("business_location", serviceProvider.getBusiness_location());
        serviceProviderDetails.put("business_email", serviceProvider.getBusiness_email());
        serviceProviderDetails.put("number_of_employees", serviceProvider.getNumber_of_employees());
        serviceProviderDetails.put("has_technical_knowledge", serviceProvider.getHas_technical_knowledge());
        serviceProviderDetails.put("work_experience_in_months", serviceProvider.getWork_experience_in_months());
        serviceProviderDetails.put("highest_qualification", serviceProvider.getHighest_qualification());
        serviceProviderDetails.put("name_of_institute", serviceProvider.getName_of_institute());
        serviceProviderDetails.put("year_of_passing", serviceProvider.getYear_of_passing());
        serviceProviderDetails.put("board_or_university", serviceProvider.getBoard_or_university());
        serviceProviderDetails.put("total_marks", serviceProvider.getTotal_marks());
        serviceProviderDetails.put("marks_obtained", serviceProvider.getMarks_obtained());
        serviceProviderDetails.put("cgpa", serviceProvider.getCgpa());
        serviceProviderDetails.put("latitude", serviceProvider.getLatitude());
        serviceProviderDetails.put("longitude", serviceProvider.getLongitude());
        serviceProviderDetails.put("rank", serviceProvider.getRank());
        serviceProviderDetails.put("signedUp", serviceProvider.getSignedUp());
       /* serviceProviderDetails.put("skills", serviceProvider.getSkills());*/
       /* serviceProviderDetails.put("infra", serviceProvider.getInfra());
        serviceProviderDetails.put("languages", serviceProvider.getLanguages());*/
/*        serviceProviderDetails.put("privileges", serviceProvider.getPrivileges());
        serviceProviderDetails.put("spAddresses", serviceProvider.getSpAddresses());*/
        return serviceProviderDetails;
    }

    public List<Map<String, Object>> mapQualifications(List<QualificationDetails> qualificationDetails) {
        return qualificationDetails.stream()
                .map(qualificationDetail -> {
                    Map<String, Object> qualificationInfo = new HashMap<>();

                    // Fetch the qualification by qualification_id
                    DocumentType qualification = entityManager.find(DocumentType.class, qualificationDetail.getQualification_id());

                    // Populate the map with necessary fields from qualificationDetail
                    qualificationInfo.put("institution_name", qualificationDetail.getInstitution_name());
                    qualificationInfo.put("year_of_passing", qualificationDetail.getYear_of_passing());
                    qualificationInfo.put("board_or_university", qualificationDetail.getBoard_or_university());
                    qualificationInfo.put("subject_name", qualificationDetail.getSubject_name());
                    qualificationInfo.put("stream",qualificationDetail.getStream());
                    qualificationInfo.put("examination_roll_number",qualificationDetail.getExamination_role_number());
                    qualificationInfo.put("examination_registration_number",qualificationDetail.getExamination_registration_number());
                    qualificationInfo.put("grade_or_percentage_value", qualificationDetail.getGrade_or_percentage_value());
                    qualificationInfo.put("marks_total", qualificationDetail.getTotal_marks());
                    qualificationInfo.put("marks_obtained", qualificationDetail.getMarks_obtained());

                    // Replace the qualification_id with qualification_name
                    if (qualification != null) {
                        qualificationInfo.put("qualification_name", qualification.getDocument_type_name());
                    } else {
                        qualificationInfo.put("qualification_name", "Unknown Qualification");
                    }

                    return qualificationInfo;
                }).collect(Collectors.toList());
    }



}

