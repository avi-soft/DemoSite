package com.community.api.component;

public class Constant {
    public static String COUNTRY_CODE="+91";
    public static String PHONE_QUERY = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode";
    public static String PHONE_QUERY_OTP = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode AND c.otp=:otp";
    public static String ID_QUERY = "SELECT c FROM CustomCustomer c WHERE c.customer_id = :customer_id";
    public static String PHONE_QUERY_SERVICE_PROVIDER = "SELECT c FROM ServiceProviderEntity c WHERE c.primaryMobileNumber = :primaryMobileNumber AND c.countryCode = :countryCode";
    //role for creating
    private static String USERNAME_QUERY_SERVICE_PROVIDER="SELECT c FROM ServiceProviderEntity c WHERE c.username = :username";
    public static final String ADMIN = "ADMIN";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String SERVICE_PROVIDER = "SERVICE_PROVIDER";
    public static final String USER = "USER";

}
