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
    public static String USERNAME_QUERY_SERVICE_PROVIDER = "SELECT c FROM ServiceProviderEntity c WHERE c.user_name = :username";
    public static final String ADMIN = "ADMIN";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String SERVICE_PROVIDER = "SERVICE_PROVIDER";
    public static final String USER = "USER";
    public static final int INITIAL_STATUS=1;
    public static String STATE_CODE_QUERY = "SELECT s FROM StateCode s WHERE s.state_name = :state_name";
    public static final String SP_USERNAME_QUERY = "SELECT s FROM ServiceProviderEntity s WHERE s.user_name LIKE :username";
    public static final String SP_EMAIL_QUERY = "SELECT s FROM ServiceProviderEntity s WHERE s.primary_email LIKE :email";
    public static final String jpql = "SELECT a FROM ServiceProviderAddressRef a";
    public static String DISTRICT_QUERY = "SELECT d.district from Districts d WHERE d.state_code = :state_code";
    public static String FIND_DISTRICT = "SELECT d.district_name from Districts d where d.district_id = :district_id";
    public static String FIND_STATE = "SELECT s.state_name from StateCode s where s.state_id = :state_id";
    public static String FETCH_ROLE = "SELECT r.role_name FROM Role r WHERE r.role_id = :role_id";
    public static String roleUser = "CUSTOMER";
    public static String roleServiceProvider = "SERVICE_PROVIDER";
    public static String GET_SKILLS_COUNT = "SELECT COUNT(*) FROM Skill";
    public static String GET_ALL_SKILLS = "SELECT s FROM Skill s";
    public static String GET_LANGUAGES_COUNT = "SELECT COUNT(*) FROM ServiceProviderLanguage";
    public static String GET_ALL_LANGUAGES = "SELECT s FROM ServiceProviderLanguage s";
    public static String OTP_SERVICE_PROVIDER = "SELECT c.otp FROM ServiceProviderEntity c WHERE c.mobileNumber = :mobileNumber";
    public static String serviceProviderRoles = "SELECT c.privilege_id FROM service_provider_privileges c WHERE c.service_provider_id = :serviceProviderId";
    public static String GET_PRIVILEGES_COUNT = "SELECT COUNT(*) FROM Privileges";
    public static String GET_ALL_PRIVILEGES = "SELECT p FROM Privileges s";
    public static String GET_INFRA_COUNT = "SELECT COUNT(*) FROM ServiceProviderInfra";
    public static String GET_INFRA_LIST = "SELECT s FROM ServiceProviderInfra s";
    public static String GET_SERVICE_PROVIDER_DEFAULT_ADDRESS="SELECT a from ServiceProviderAddressRef a where address_name =:address_name";
    public static String GET_COUNT_OF_ROLES="Select COUNT(*) from Role";
    public static String GET_COUNT_OF_STATUS="Select COUNT(*) from ServiceProviderStatus";
    public static String GET_ALL_STATUS="Select s from ServiceProviderStatus s";
    public static String GET_ALL_ROLES="Select r from Role r";

}
