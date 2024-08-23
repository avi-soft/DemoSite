package com.community.api.component;

public class Constant {
    public static String COUNTRY_CODE = "+91";
    public static String PHONE_QUERY = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode";
    public static String PHONE_QUERY_OTP = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber AND c.countryCode = :countryCode AND c.otp=:otp";
    public static String ID_QUERY = "SELECT c FROM CustomCustomer c WHERE c.customer_id = :customer_id";
    public static final String FIND_ALL_QUALIFICATIONS_QUERY = "SELECT q FROM Qualification q";
    public static final String FIND_ALL_EXAMINATIONS_QUERY = "SELECT q FROM Examination q";
    public static final String FIND_EXAMINATION_BY_NAME_QUERY = "SELECT e FROM Examination e WHERE e.examinationName = :examinationName";
    public static String PHONE_QUERY_SERVICE_PROVIDER = "SELECT c FROM ServiceProviderEntity c WHERE c.mobileNumber = :mobileNumber AND c.country_code = :country_code";

    //role for creating
    public static String USERNAME_QUERY_SERVICE_PROVIDER = "SELECT c FROM ServiceProviderEntity c WHERE c.user_name = :username";
    public static final String ADMIN = "ADMIN";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String SERVICE_PROVIDER = "SERVICE_PROVIDER";
    public static final String USER = "USER";
    public static String STATE_CODE_QUERY = "SELECT s FROM StateCode s WHERE s.state_name = :state_name";
    public static final String SP_USERNAME_QUERY = "SELECT s FROM ServiceProviderEntity s WHERE s.user_name LIKE :username";
    public static final String SP_EMAIL_QUERY = "SELECT s FROM ServiceProviderEntity s WHERE s.primary_email LIKE :email";
    public static final String jpql = "SELECT a FROM ServiceProviderAddressRef a";
    public static String DISTRICT_QUERY = "SELECT d.district from Districts d WHERE d.state_code = :state_code";
    public static String FIND_DISTRICT = "SELECT d.district_name from Districts d where d.district_id = :district_id";
    public static String FIND_STATE = "SELECT s.state_name from StateCode s where s.state_id = :state_id";
    public static String FETCH_ROLE = "SELECT r.role_name FROM Role r WHERE r.role_id = :role_id";
    public static String roleUser="CUSTOMER";
    public static String roleServiceProvider="SERVICE_PROVIDER";
    public static String OTP_SERVICE_PROVIDER = "SELECT c.otp FROM ServiceProviderEntity c WHERE c.mobileNumber = :mobileNumber";
}