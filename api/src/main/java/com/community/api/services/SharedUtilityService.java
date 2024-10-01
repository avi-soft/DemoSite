package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.endpoint.customer.AddressDTO;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomerAddressDTO;
import com.community.api.entity.Skill;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.OrderService;
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

@Service
public class SharedUtilityService {
    private EntityManager entityManager;
    private ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public OrderService orderService;

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
        Order cart=orderService.findCartForCustomer(customer);
        if(cart!=null)
        customerDetails.put("orderId",cart.getId());
        else
            customerDetails.put("orderId",null);
        customerDetails.put("mobileNumber", customCustomer.getMobileNumber());
        customerDetails.put("secondaryMobileNumber", customCustomer.getSecondaryMobileNumber());
        customerDetails.put("whatsappNumber", customCustomer.getWhatsappNumber());

        customerDetails.put("countryCode", customCustomer.getCountryCode());
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

        customerDetails.put("secondaryEmail", customCustomer.getSecondaryEmail());
        customerDetails.put("mothers_name", customCustomer.getMothersName());
        customerDetails.put("date_of_birth", customCustomer.getDob());
        customerDetails.put("adhar_number", customCustomer.getAdharNumber());
        customerDetails.put("category_issue_date", customCustomer.getCategoryIssueDate());
        customerDetails.put("height_cms", customCustomer.getHeightCms());
        customerDetails.put("weight_kgs", customCustomer.getWeightKgs());
        customerDetails.put("chest_size_cms", customCustomer.getChestSizeCms());
        customerDetails.put("shoe_size_inches", customCustomer.getShoeSizeInches());
        customerDetails.put("waist_size_cms", customCustomer.getWaistSizeCms());
        customerDetails.put("can_swim", customCustomer.getCanSwim());
        customerDetails.put("proficiency_in_sports_national_level", customCustomer.getProficiencyInSportsNationalLevel());
        customerDetails.put("first_choice_exam_city", customCustomer.getFirstChoiceExamCity());
        customerDetails.put("second_choice_exam_city", customCustomer.getSecondChoiceExamCity());
        customerDetails.put("third_choice_exam_city", customCustomer.getThirdChoiceExamCity());
        customerDetails.put("mphil_passed", customCustomer.getMphilPassed());
        customerDetails.put("phd_passed", customCustomer.getPhdPassed());
        customerDetails.put("number_of_attempts", customCustomer.getNumberOfAttempts());
        customerDetails.put("work_experience", customCustomer.getWorkExperience());
        customerDetails.put("category_valid_upto", customCustomer.getCategoryValidUpto());
        customerDetails.put("religion", customCustomer.getReligion());
        customerDetails.put("belongs_to_minority", customCustomer.getBelongsToMinority());
        customerDetails.put("secondary_mobile_number", customCustomer.getSecondaryMobileNumber());
        customerDetails.put("whatsapp_number", customCustomer.getWhatsappNumber());
        customerDetails.put("secondary_email", customCustomer.getSecondaryEmail());
        customerDetails.put("disability_handicapped", customCustomer.getDisability());
        customerDetails.put("is_ex_service_man", customCustomer.getExService());
        customerDetails.put("is_married", customCustomer.getIsMarried());
        customerDetails.put("visible_identification_mark_1", customCustomer.getIdentificationMark1());
        customerDetails.put("visible_identification_mark_2", customCustomer.getIdentificationMark2());

        Map<String,String>currentAddress=new HashMap<>();
        Map<String,String>permanentAddress=new HashMap<>();
        for(CustomerAddress customerAddress:customer.getCustomerAddresses())
        {
            if(customerAddress.getAddressName().equals("CURRENT_ADDRESS"))
            {
                currentAddress.put("state", customerAddress.getAddress().getStateProvinceRegion());
                currentAddress.put("city", customerAddress.getAddress().getCity());
                currentAddress.put("district", customerAddress.getAddress().getCounty());
                currentAddress.put("pincode", customerAddress.getAddress().getPostalCode());
                currentAddress.put("Address line",customerAddress.getAddress().getAddressLine1());
            }
            if(customerAddress.getAddressName().equals("PERMANENT_ADDRESS"))
            {
                permanentAddress.put("state", customerAddress.getAddress().getStateProvinceRegion());
                permanentAddress.put("city", customerAddress.getAddress().getCity());
                permanentAddress.put("district", customerAddress.getAddress().getCounty());
                permanentAddress.put("pincode", customerAddress.getAddress().getPostalCode());
                permanentAddress.put("Address line",customerAddress.getAddress().getAddressLine1());
            }

        }
        customerDetails.put("currentAddress",currentAddress);
        customerDetails.put("permanentAddress",permanentAddress);
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



}

