package com.community.api.services;

import org.springframework.stereotype.Service;

@Service
public class ApiConstants {
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";

    public static final String MOBILE_NUMBER_NULL_OR_EMPTY = "Mobile number cannot be null or empty";
    public static final String NUMBER_ALREADY_REGISTERED_SERVICE_PROVIDER = "Number already registered as Service Provider";
    public static final String OTP_SENT_SUCCESSFULLY = "OTP has been sent successfully";

    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access: Please check your API key";
    public static final String INTERNAL_SERVER_ERROR = "Internal server error occurred";
    public static final String ERROR_SENDING_OTP = "Error sending OTP: ";

    // Exception Messages
    public static final String SOME_EXCEPTION_OCCURRED = "Some Exception Occurred";
    public static final String NUMBER_FORMAT_EXCEPTION = "Number Format Exception";
    public static final String CATALOG_SERVICE_NOT_INITIALIZED = "Catalog Service Not Initialized";
    public static final String MOBILE_NUMBER_REGISTERED = " Number is already exists as Service Provider";
    public static final String INVALID_MOBILE_NUMBER = "Invalid Mobile Number";
    public static final String CUSTOMER_ALREADY_EXISTS =" Customer Already Exists";
    public static final String RATE_LIMIT_EXCEEDED = "Rate Limit Exceeded. Please try after some time";

    public static final String INVALID_DATA = "Invalid data provided";
    public static final String NUMBER_REGISTERED_AS_CUSTOMER = "Number already registered as customer";
    public static final String NO_RECORDS_FOUND = "No records found";
    public static final String INVALID_ROLE = "Invalid Role";
    public static final String CUSTOMER_SERVICE_NOT_INITIALIZED = "Customer Service Not Initialized";
    public static final String ROLE_EMPTY = "Role cannot be empty";
    ;
    ;
    ;
}
