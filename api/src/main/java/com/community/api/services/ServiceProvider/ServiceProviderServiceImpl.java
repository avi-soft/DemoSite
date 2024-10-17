package com.community.api.services.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.entity.*;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.entity.ServiceProviderAddress;
import com.community.api.entity.ServiceProviderAddressRef;
import com.community.api.entity.ServiceProviderInfra;
import com.community.api.entity.ServiceProviderLanguage;
import com.community.api.entity.Skill;
import com.community.api.entity.StateCode;
import com.community.api.services.ApiConstants;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.DistrictService;
import com.community.api.services.RateLimiterService;
import com.community.api.services.ResponseService;
import com.community.api.services.ServiceProviderInfraService;
import com.community.api.services.ServiceProviderLanguageService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.SkillService;
import com.community.api.services.TwilioServiceForServiceProvider;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import io.github.bucket4j.Bucket;
import io.micrometer.core.lang.Nullable;
import org.apache.zookeeper.server.SessionTracker;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;


import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.validation.constraints.Pattern;

@Service
public class ServiceProviderServiceImpl implements ServiceProviderService {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private CustomCustomerService customCustomerService;
    @Autowired
    private CustomerService customerService;
    @Value("${twilio.accountSid}")
    private String accountSid;
    @Value("${twilio.authToken}")
    private String authToken;
    @Autowired
    private TwilioServiceForServiceProvider twilioService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private ServiceProviderInfraService serviceProviderInfraService;
    @Autowired
    private ServiceProviderLanguageService serviceProviderLanguageService;
    @Autowired
    private RateLimiterService rateLimiterService;
    @Autowired
    private SharedUtilityService sharedUtilityService;

    @Value("${twilio.phoneNumber}")
    private String twilioPhoneNumber;

    @Override
    @Transactional
    public ServiceProviderEntity saveServiceProvider(ServiceProviderEntity serviceProviderEntity) {
        try {
            entityManager.persist(serviceProviderEntity);
            ServiceProviderStatus serviceProviderStatus = entityManager.find(ServiceProviderStatus.class, 1);
            serviceProviderEntity.setStatus(serviceProviderStatus);
            return serviceProviderEntity;
        } catch (Exception e) {
            throw new RuntimeException("Error saving service provider entity", e);
        }
    }


    @Transactional
    public ResponseEntity<?> updateServiceProvider(Long userId, Map<String, Object> updates)  {
        try{
            updates=sharedUtilityService.trimStringValues(updates);
            List<String> errorMessages=new ArrayList<>();


        // Find existing ServiceProviderEntity
        ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, userId);
        if (existingServiceProvider == null) {
            errorMessages.add("ServiceProvider with ID " + userId + " not found");
        }

            if (updates.containsKey("type")) {
                String typeStr = (String) updates.get("type");

                // Validate that the type value is either "Professional" or "Individual"
                if(typeStr==null || typeStr.trim().isEmpty())
                {
                    return responseService.generateErrorResponse("Service Provider type cannot be null or empty", HttpStatus.BAD_REQUEST);
                }
                if (!typeStr.equalsIgnoreCase("PROFESSIONAL") && !typeStr.equalsIgnoreCase("INDIVIDUAL")) {
                    return responseService.generateErrorResponse("Invalid value for 'type'. Allowed values are 'PROFESSIONAL' or 'INDIVIDUAL'.", HttpStatus.BAD_REQUEST);
                }
                existingServiceProvider.setType(typeStr.toUpperCase());
                updates.remove("type");
            }


        // Validate and check for unique constraints
        ServiceProviderEntity existingSPByUsername = null;
        ServiceProviderEntity existingSPByEmail = null;

        if (updates.containsKey("user_name")) {
            updates.remove("user_name");
        }
        if (updates.containsKey("primary_mobile_number")) {
            String userName = (String) updates.get("user_name");
            existingSPByUsername = findServiceProviderByUserName(userName);
        }

        if (updates.containsKey("primary_email")) {
            String primaryEmail = (String) updates.get("primary_email");
            existingSPByEmail = findSPbyEmail(primaryEmail);
        }

        if ((existingSPByUsername != null) || existingSPByEmail != null) {
            if (existingSPByUsername != null && !existingSPByUsername.getService_provider_id().equals(userId)) {
                return responseService.generateErrorResponse("Username is not available", HttpStatus.BAD_REQUEST);
            }
            if (existingSPByEmail != null && !existingSPByEmail.getService_provider_id().equals(userId)) {
                return responseService.generateErrorResponse("Email not available", HttpStatus.BAD_REQUEST);
            }
        }
        List<Skill> serviceProviderSkills = new ArrayList<>();
        List<ServiceProviderInfra> serviceProviderInfras = new ArrayList<>();
        List<ServiceProviderLanguage> serviceProviderLanguages = new ArrayList<>();
        List<Integer> infraList = getIntegerList(updates, "infra_list");
        List<Integer> skillList = getIntegerList(updates, "skill_list");
        List<Integer> languageList = getIntegerList(updates, "language_list");
        if (updates.containsKey("has_technical_knowledge")) {
            if ((boolean) updates.get("has_technical_knowledge").equals(true)) {
                if (!skillList.isEmpty()) {
                    for (int skill_id : skillList) {
                        Skill skill = entityManager.find(Skill.class, skill_id);
                        if (skill != null) {
                            if (!serviceProviderSkills.contains(skill))
                                serviceProviderSkills.add(skill);
                        }
                    }
                }
            }
        } else
            existingServiceProvider.setSkills(null);
        if (!infraList.isEmpty()) {
            for (int infra_id : infraList) {
                ServiceProviderInfra serviceProviderInfrastructure = entityManager.find(ServiceProviderInfra.class, infra_id);
                if (serviceProviderInfrastructure != null) {
                    if (!serviceProviderInfras.contains(serviceProviderInfrastructure))
                        serviceProviderInfras.add(serviceProviderInfrastructure);
                }
            }
        }
        if (!languageList.isEmpty()) {
            for (int language_id : languageList) {
                ServiceProviderLanguage serviceProviderLanguage = entityManager.find(ServiceProviderLanguage.class, language_id);
                if (serviceProviderLanguage != null) {
                    if (!serviceProviderLanguages.contains(serviceProviderLanguage))
                        serviceProviderLanguages.add(serviceProviderLanguage);
                }
            }
        }
        existingServiceProvider.setInfra(serviceProviderInfras);
        existingServiceProvider.setSkills(serviceProviderSkills);
        existingServiceProvider.setLanguages(serviceProviderLanguages);
        updates.remove("skill_list");
        updates.remove("infra_list");
        updates.remove("language_list");
        if(updates.containsKey("district")&&updates.containsKey("state")/*&&updates.containsKey("city")*/&&updates.containsKey("pincode")&&updates.containsKey("residential_address"))
        {
            if(validateAddressFields(updates).isEmpty()) {
                if (existingServiceProvider.getSpAddresses().isEmpty()) {
                    ServiceProviderAddress serviceProviderAddress = new ServiceProviderAddress();
                    serviceProviderAddress.setAddress_type_id(findAddressName("CURRENT_ADDRESS").getAddress_type_Id());
                    serviceProviderAddress.setPincode((String) updates.get("pincode"));
                    serviceProviderAddress.setDistrict((String) updates.get("district"));
                    serviceProviderAddress.setState((String) updates.get("state"));
                    /*serviceProviderAddress.setCity((String) updates.get("city"));*/
                    serviceProviderAddress.setAddress_line((String) updates.get("residential_address"));
                    if (serviceProviderAddress.getAddress_line() != null /*|| serviceProviderAddress.getCity() != null*/ || serviceProviderAddress.getDistrict() != null || serviceProviderAddress.getState() != null || serviceProviderAddress.getPincode() != null) {
                        addAddress(existingServiceProvider.getService_provider_id(), serviceProviderAddress);
                    }
                } else {
                    ServiceProviderAddress serviceProviderAddress = existingServiceProvider.getSpAddresses().get(0);
                    ServiceProviderAddress serviceProviderAddressDTO = new ServiceProviderAddress();
                    serviceProviderAddressDTO.setAddress_type_id(serviceProviderAddress.getAddress_type_id());
                    serviceProviderAddressDTO.setAddress_id(serviceProviderAddress.getAddress_id());
                    serviceProviderAddressDTO.setState((String) updates.get("state"));
                    serviceProviderAddressDTO.setDistrict((String) updates.get("district"));
                    serviceProviderAddressDTO.setAddress_line((String) updates.get("residential_address"));
                    serviceProviderAddressDTO.setPincode((String) updates.get("pincode"));
                    serviceProviderAddressDTO.setServiceProviderEntity(existingServiceProvider);
                    /*serviceProviderAddressDTO.setCity((String) updates.get("city"));*/
                    for (String error : updateAddress(existingServiceProvider.getService_provider_id(), serviceProviderAddress, serviceProviderAddressDTO)) {
                        errorMessages.add(error);
                    }
                }
            }else
            {
                errorMessages.addAll(validateAddressFields(updates));
            }
        }

        //removing key for address
        updates.remove("residential_address");
        updates.remove("city");
        updates.remove("state");
        updates.remove("district");
        updates.remove("pincode");

        // Update only the fields that are present in the map using reflections
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String fieldName = entry.getKey();
            Object newValue = entry.getValue();

                Field field = ServiceProviderEntity.class.getDeclaredField(fieldName);
                System.out.println(field);
                Column columnAnnotation = field.getAnnotation(Column.class);
                boolean isColumnNotNull = (columnAnnotation != null && !columnAnnotation.nullable());
                // Check if the field has the @Nullable annotation
                boolean isNullable = field.isAnnotationPresent(Nullable.class);
                field.setAccessible(true);
                if(newValue.toString().isEmpty() && !isNullable)
                    errorMessages.add(fieldName+ " cannot be null");
                if(newValue.toString().isEmpty()&& isNullable)
                    continue;
                if(newValue!=null) {
                    if (field.isAnnotationPresent(Size.class)) {
                        Size sizeAnnotation = field.getAnnotation(Size.class);
                        int min = sizeAnnotation.min();
                        int max = sizeAnnotation.max();
                        if (newValue.toString().length() > max || newValue.toString().length() < min) {
                            if (max == min)
                                errorMessages.add(fieldName + " size should be of size " + max);
                            else
                                errorMessages.add(fieldName + " size should be in between " + min + " " + max);
                            continue;
                        }
                    }
                    if (field.isAnnotationPresent(Email.class)) {
                        Email emailAnnotation=field.getAnnotation(Email.class);
                        String message=emailAnnotation.message();
                        if(fieldName.equals("primary_email"))
                        {
                            if(newValue.equals((String)updates.get("secondary_email"))||(existingServiceProvider.getSecondary_email()!=null&&newValue.equals(existingServiceProvider.getSecondary_email())))
                                errorMessages.add("primary and secondary email cannot be same");
                        }
                        else if(fieldName.equals("secondary_email"))
                        {
                            if(newValue.equals((String)updates.get("primary_email"))||(existingServiceProvider.getPrimary_email()!=null&&newValue.equals(existingServiceProvider.getPrimary_email())))
                                errorMessages.add("primary and secondary email cannot be same");
                        }
                        if(!sharedUtilityService.isValidEmail((String)newValue)) {
                            errorMessages.add(message.replace("{field}", fieldName));
                            continue;
                        }
                    }

                    if (field.isAnnotationPresent(Pattern.class)) {
                        Pattern patternAnnotation = field.getAnnotation(Pattern.class);
                        String regex = patternAnnotation.regexp();
                        String message = patternAnnotation.message(); // Get custom message
                        if (!newValue.toString().matches(regex)) {
                            errorMessages.add(fieldName+ "is invalid"); // Use a placeholder
                            continue;
                        }
                    }
                }
                field.setAccessible(true);
                // Optionally, check for type compatibility before setting the value
                if (newValue != null && field.getType().isAssignableFrom(newValue.getClass())) {
                    field.set(existingServiceProvider, newValue);
                }
        }
        if(!errorMessages.isEmpty())
            return ResponseService.generateErrorResponse(errorMessages.toString(),HttpStatus.BAD_REQUEST);
            // Merge the updated entity
        entityManager.merge(existingServiceProvider);
        if (existingServiceProvider.getUser_name() == null && !existingServiceProvider.getSpAddresses().isEmpty() ) {
            String username = generateUsernameForServiceProvider(existingServiceProvider);
            existingServiceProvider.setUser_name(username);
        }
        entityManager.merge(existingServiceProvider);
        return responseService.generateSuccessResponse("Service Provider Updated Successfully", existingServiceProvider, HttpStatus.OK);
    }catch (NoSuchFieldException e)
        {
            return ResponseService.generateErrorResponse("No such field present :"+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error updating Service Provider : ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<String> validateAddressFields(Map<String,Object>updates)
    {
        List<String> errorMessages = new ArrayList<>();
        String state = (String) updates.get("state");
        String district = (String) updates.get("district");
        String pincode = (String) updates.get("pincode");
       /* String city = (String) updates.get("city");*/
        String residentialAddress = (String) updates.get("residential_address");
        String[] fieldNames = {"state", "district", "pincode", "residential_address"};
        String[] fieldValues = {state, district, pincode, residentialAddress};
        for (int i = 0; i < fieldValues.length; i++) {
            if (fieldValues[i] == null || fieldValues[i].trim().isEmpty()) {
                errorMessages.add(fieldNames[i] + " cannot be empty");
            }
        }
        String pattern = Constant.PINCODE_REGEXP;
        if(!java.util.regex.Pattern.matches(pattern,pincode))
            errorMessages.add("Pincode should contain only numbers and should be of length 6");
       /* pattern = Constant.CITY_REGEXP;
        if(!java.util.regex.Pattern.matches(pattern, city))
            errorMessages.add("Field city should only contain letters");*/
        return errorMessages;
    }

    @Override
    public ServiceProviderEntity getServiceProviderById(Long userId) {
        return entityManager.find(ServiceProviderEntity.class, userId);
    }

    @Transactional
    public ResponseEntity<?> sendOtpToMobile(String mobileNumber, String countryCode) {

        if (mobileNumber == null || mobileNumber.isEmpty()) {
            throw new IllegalArgumentException("Mobile number cannot be null or empty");

        }

        try {
            Twilio.init(accountSid, authToken);
            String completeMobileNumber = Constant.COUNTRY_CODE + mobileNumber;
            String otp = generateOTP();


/*            Message message = Message.creator(

                            new PhoneNumber(completeMobileNumber),
                            new PhoneNumber(twilioPhoneNumber),
                            otp)


                    .create();
*/


            ServiceProviderEntity existingServiceProvider = findServiceProviderByPhone(mobileNumber, countryCode);
            if (existingServiceProvider == null) {
                ServiceProviderEntity serviceProviderEntity = new ServiceProviderEntity();
                serviceProviderEntity.setService_provider_id(customerService.findNextCustomerId());
                serviceProviderEntity.setCountry_code(Constant.COUNTRY_CODE);
                serviceProviderEntity.setMobileNumber(mobileNumber);
                serviceProviderEntity.setOtp(otp);
                entityManager.persist(serviceProviderEntity);

            } else {
                existingServiceProvider.setOtp(otp);
                entityManager.merge(existingServiceProvider);
            }


            return responseService.generateSuccessResponse("OTP has been sent successfully !!!", otp, HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return responseService.generateErrorResponse("Unauthorized access: Please check your API key", HttpStatus.UNAUTHORIZED);
            } else {
                exceptionHandling.handleHttpClientErrorException(e);
                return responseService.generateErrorResponse("Internal server error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return responseService.generateErrorResponse("Error sending OTP: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error sending OTP: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public synchronized String generateOTP() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(8999);
        return String.valueOf(otp);
    }


    @Transactional
    public boolean setotp(String mobileNumber, String countryCode) {
        ServiceProviderEntity exisitingServiceProvider = findServiceProviderByPhone(mobileNumber, countryCode);

        if (exisitingServiceProvider != null) {
            String storedOtp = exisitingServiceProvider.getOtp();
            if (storedOtp != null) {
                exisitingServiceProvider.setOtp(null);
                entityManager.merge(exisitingServiceProvider);
                return true;
            }
        }
        return false;
    }

    public boolean isValidMobileNumber(String mobileNumber) {

        if (mobileNumber.startsWith("0")) {
            mobileNumber = mobileNumber.substring(1);
        }
        String mobileNumberPattern = "^\\d{9,13}$";
        return java.util.regex.Pattern.compile(mobileNumberPattern).matcher(mobileNumber).matches();
    }

    public ServiceProviderEntity findServiceProviderByPhone(String mobileNumber, String countryCode) {

        return entityManager.createQuery(Constant.PHONE_QUERY_SERVICE_PROVIDER, ServiceProviderEntity.class)
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("country_code", countryCode)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public ServiceProviderEntity findServiceProviderByUserName(String username) {

        return entityManager.createQuery(Constant.USERNAME_QUERY_SERVICE_PROVIDER, ServiceProviderEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    //find service provider by phone and validate the password.
    public ResponseEntity<?> authenticateByPhone(String mobileNumber, String countryCode, String password, HttpServletRequest request, HttpSession session) {
        ServiceProviderEntity existingServiceProvider = findServiceProviderByPhone(mobileNumber, countryCode);
        return validateServiceProvider(existingServiceProvider, password, request, session);
    }

    //find service provider by username and validate the password.
    public ResponseEntity<?> authenticateByUsername(String username, String password, HttpServletRequest request, HttpSession session) {
        ServiceProviderEntity existingServiceProvider = findServiceProviderByUserName(username);
        return validateServiceProvider(existingServiceProvider, password, request, session);
    }

    //mechanism to check password
    public ResponseEntity<?> validateServiceProvider(ServiceProviderEntity serviceProvider, String password, HttpServletRequest request, HttpSession session) {
        if (serviceProvider == null) {
            return responseService.generateErrorResponse("No Records Found", HttpStatus.NOT_FOUND);
        }
        if (passwordEncoder.matches(password, serviceProvider.getPassword())) {
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String tokenKey = "authTokenServiceProvider_" + serviceProvider.getMobileNumber();


            String existingToken = serviceProvider.getToken();

            Map<String,Object> serviceProviderResponse= sharedUtilityService.serviceProviderDetailsMap(serviceProvider);


            if(existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {

                Map<String, Object> responseBody = createAuthResponse(existingToken, serviceProviderResponse).getBody();

                return ResponseEntity.ok(responseBody);
            } else {
                String newToken = jwtUtil.generateToken(serviceProvider.getService_provider_id(), serviceProvider.getRole(), ipAddress, userAgent);
                session.setAttribute(tokenKey, newToken);

                serviceProvider.setToken(newToken);
                entityManager.persist(serviceProvider);

                Map<String, Object> responseBody = createAuthResponse(newToken, serviceProviderResponse).getBody();


            return ResponseEntity.ok(responseBody);


            }
        } else {
            return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> loginWithPassword(@RequestBody Map<String, Object> serviceProviderDetails, HttpServletRequest request, HttpSession session) {
        try {
            String mobileNumber = (String) serviceProviderDetails.get("mobileNumber");
            if(mobileNumber!=null) {
                if (mobileNumber.startsWith("0"))
                    mobileNumber = mobileNumber.substring(1);
            }

            String username = (String) serviceProviderDetails.get("username");
            String password = (String) serviceProviderDetails.get("password");
            String countryCode = (String) serviceProviderDetails.getOrDefault("countryCode", Constant.COUNTRY_CODE);
            // Check for empty password
            if (password == null || password.isEmpty()) {
                return responseService.generateErrorResponse("Password cannot be empty", HttpStatus.BAD_REQUEST);

            }
            if (mobileNumber != null && !mobileNumber.isEmpty()) {
                return authenticateByPhone(mobileNumber, countryCode, password, request, session);
            } else if (username != null && !username.isEmpty()) {
                return authenticateByUsername(username, password, request, session);
            } else {
                return responseService.generateErrorResponse("Empty Phone Number or username", HttpStatus.BAD_REQUEST);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> loginWithUsernameAndOTP(String username, HttpSession session) {
        try {
            if (username == null) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);

            }
            ServiceProviderEntity existingServiceProivder = findServiceProviderByUserName(username);
            if (existingServiceProivder == null) {
                return responseService.generateErrorResponse("No records found", HttpStatus.NOT_FOUND);


            }
            if (existingServiceProivder.getMobileNumber() == null) {
                return responseService.generateErrorResponse("No mobile Number registerd for this account", HttpStatus.NOT_FOUND);

            }
            String countryCode = existingServiceProivder.getCountry_code();
            if (countryCode == null)
                countryCode = Constant.COUNTRY_CODE;
            return (sendOtp(existingServiceProivder.getMobileNumber(), countryCode, session));
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> sendOtp(String mobileNumber, String countryCode, HttpSession session) throws UnsupportedEncodingException {
        try {
            mobileNumber = mobileNumber.startsWith("0")
                    ? mobileNumber.substring(1)
                    : mobileNumber;
            if (countryCode == null)
                countryCode = Constant.COUNTRY_CODE;
            Bucket bucket = rateLimiterService.resolveBucket(mobileNumber, "/service-provider/otp/send-otp");
            if (bucket.tryConsume(1)) {
                if (!isValidMobileNumber(mobileNumber)) {
                    return responseService.generateErrorResponse("Invalid mobile number", HttpStatus.BAD_REQUEST);

                }
                ResponseEntity<?> otpResponse = twilioService.sendOtpToMobile(mobileNumber, countryCode);
                return otpResponse;
            } else {
                return responseService.generateErrorResponse("You can send OTP only once in 1 minute", HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);

            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public String generateUsernameForServiceProvider(ServiceProviderEntity serviceProviderDetails) {
        String firstName = serviceProviderDetails.getFirst_name();
        String lastName = serviceProviderDetails.getLast_name();
        String state = serviceProviderDetails.getSpAddresses().get(0).getState();
        String username = null;
        StateCode stateDetails;
        if (firstName != null && lastName != null && state != null) {
            stateDetails = findStateCode(state);
            username = stateDetails.getState_code() + firstName + lastName;
            //suffix check
            //if a user already exist with username like PBRajSharma
            if (!findServiceProviderListByUsername(username).isEmpty()) {
                List<ServiceProviderEntity> listOfSp = findServiceProviderListByUsername(username);
                ServiceProviderEntity serviceProvider = listOfSp.get(listOfSp.size() - 1);
                String suffix = serviceProvider.getUser_name().substring(serviceProvider.getUser_name().length() - 2);
                int suffixValue = Integer.parseInt(suffix);
                if (suffixValue < 9)
                    username = username + "0" + Integer.toString(suffixValue + 1);
                else
                    username = username + Integer.toString(suffixValue + 1);
            }
            //simply adding 01 if there are no users for the given username
            else
                username = username + "01";
        }
        return username;
    }

    @Transactional
    public ResponseEntity<?> verifyOtp(Map<String, Object> serviceProviderDetails, HttpSession session, HttpServletRequest request) {
        try {
            String username = (String) serviceProviderDetails.get("username");
            String otpEntered = (String) serviceProviderDetails.get("otpEntered");
            String mobileNumber = (String) serviceProviderDetails.get("mobileNumber");
            String countryCode = (String) serviceProviderDetails.get("countryCode");
            Integer role = (Integer) serviceProviderDetails.get("role");
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }

            if (username != null) {
                ServiceProviderEntity serviceProvider = findServiceProviderByUserName(username);
                if (serviceProvider == null) {
                    return responseService.generateErrorResponse("No records found ", HttpStatus.NOT_FOUND);

                }
                mobileNumber = serviceProvider.getMobileNumber();
            } else if (mobileNumber == null || mobileNumber.isEmpty()) {
                return responseService.generateErrorResponse("mobile number can not be null ", HttpStatus.BAD_REQUEST);

            }

            if (!isValidMobileNumber(mobileNumber)) {
                return responseService.generateErrorResponse("Invalid mobile number ", HttpStatus.BAD_REQUEST);

            }
            if (mobileNumber.startsWith("0"))
                mobileNumber = mobileNumber.substring(1);
            ServiceProviderEntity existingServiceProvider = findServiceProviderByPhone(mobileNumber, countryCode);

            if (existingServiceProvider == null) {
                return responseService.generateErrorResponse("Invalid Data Provided ", HttpStatus.UNAUTHORIZED);

            }

            String storedOtp = existingServiceProvider.getOtp();
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String tokenKey = "authTokenServiceProvider_" + mobileNumber;


            if (otpEntered == null || otpEntered.trim().isEmpty()) {
                return responseService.generateErrorResponse("OTP cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (otpEntered.equals(storedOtp)) {
                existingServiceProvider.setOtp(null);
                entityManager.merge(existingServiceProvider);



                String existingToken = existingServiceProvider.getToken();


                Map<String,Object> serviceProviderResponse= sharedUtilityService.serviceProviderDetailsMap(existingServiceProvider);
                if (existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {


                                        Map<String, Object> responseBody = createAuthResponse(existingToken, serviceProviderResponse).getBody();


                    return ResponseEntity.ok(responseBody);
                } else {
                    String newToken = jwtUtil.generateToken(existingServiceProvider.getService_provider_id(), role, ipAddress, userAgent);
                    session.setAttribute(tokenKey, newToken);

                    existingServiceProvider.setToken(newToken);
                    entityManager.persist(existingServiceProvider);
                    Map<String, Object> responseBody = createAuthResponse(newToken, serviceProviderResponse).getBody();
                    if(existingServiceProvider.getSignedUp()==0) {
                        existingServiceProvider.setSignedUp(1);
                        entityManager.merge(existingServiceProvider);
                        responseBody.put("message", "User has been signed up");
                    }
                    return ResponseEntity.ok(responseBody);
                }
            } else {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.UNAUTHORIZED);

            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Otp verification error" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, Object>> createAuthResponse(String token, Map<String, Object> serviceProviderEntity) {
        Map<String, Object> responseBody = new HashMap<>();

        Map<String, Object> data = new HashMap<>();
        data.put("serviceproviderDetails", serviceProviderEntity);
        responseBody.put("status_code", HttpStatus.OK.value());
        responseBody.put("data", data);
        responseBody.put("token", token);
        responseBody.put("message", "User has been logged in");
        responseBody.put("status", "OK");

        return ResponseEntity.ok(responseBody);
    }

    public StateCode findStateCode(String state_name) {

        return entityManager.createQuery(Constant.STATE_CODE_QUERY, StateCode.class)
                .setParameter("state_name", state_name)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public ServiceProviderEntity findSPbyEmail(String email) {

        return entityManager.createQuery(Constant.SP_EMAIL_QUERY, ServiceProviderEntity.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public ServiceProviderAddressRef findAddressName(String address_name) {

        return entityManager.createQuery(Constant.GET_SERVICE_PROVIDER_DEFAULT_ADDRESS, ServiceProviderAddressRef.class)
                .setParameter("address_name", address_name)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<ServiceProviderEntity> findServiceProviderListByUsername(String username) {
        username = username + "%";
        return entityManager.createQuery(Constant.SP_USERNAME_QUERY, ServiceProviderEntity.class)
                .setParameter("username", username)
                .getResultList();
    }

    public static List<Integer> getIntegerList(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;

            if (!list.isEmpty() && list.get(0) instanceof Integer) {
                return (List<Integer>) list;
            }
        }

        return Collections.emptyList();
    }

    @Transactional
    public ResponseEntity<?> addAddress(long serviceProviderId, ServiceProviderAddress serviceProviderAddress) throws Exception {
        try {
            if (serviceProviderAddress == null) {
                return responseService.generateErrorResponse("Incomplete Details", HttpStatus.BAD_REQUEST);
            }
            ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (existingServiceProvider == null) {
                return responseService.generateErrorResponse("Service Provider Not found", HttpStatus.BAD_REQUEST);
            }
            List<ServiceProviderAddress> addresses = existingServiceProvider.getSpAddresses();
            for(ServiceProviderAddress serviceProviderAddressToAdd:addresses)
            {
                if(serviceProviderAddressToAdd.getAddress_type_id()==serviceProviderAddress.getAddress_type_id())
                    return ResponseService.generateErrorResponse("Cannot add another address of this type",HttpStatus.BAD_REQUEST);
            }
            serviceProviderAddress.setState(districtService.findStateById(Integer.parseInt(serviceProviderAddress.getState())));
            serviceProviderAddress.setDistrict(districtService.findDistrictById(Integer.parseInt(serviceProviderAddress.getDistrict())));
            addresses.add(serviceProviderAddress);
            existingServiceProvider.setSpAddresses(addresses);
            serviceProviderAddress.setServiceProviderEntity(existingServiceProvider);

            entityManager.persist(serviceProviderAddress);

            entityManager.merge(existingServiceProvider);
            return responseService.generateSuccessResponse("Address added successfully", serviceProviderAddress, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error adding address", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    public List<String> updateAddress(long serviceProviderId, ServiceProviderAddress serviceProviderAddress,ServiceProviderAddress dto) throws Exception {
            List<String>errorList=new ArrayList<>();
            if (serviceProviderAddress == null) {
                errorList.add("Incomplete Details");
            }
            ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (existingServiceProvider == null) {
                errorList.add("Incomplete Details");
            }
            ServiceProviderAddress addressToupdate=null;
            List<ServiceProviderAddress> addresses = existingServiceProvider.getSpAddresses();
            if(addresses.contains(serviceProviderAddress))
            {
                for(ServiceProviderAddress iteratedAddress:addresses)
                {
                    if(iteratedAddress.getAddress_id()==serviceProviderAddress.getAddress_id())
                    {
                        addressToupdate=iteratedAddress;
                        break;
                }
                }
            }
            for (Field field : ServiceProviderAddress.class.getDeclaredFields()) {
                Column columnAnnotation = field.getAnnotation(Column.class);
                boolean isColumnNotNull = (columnAnnotation != null && !columnAnnotation.nullable());
                // Check if the field has the @Nullable annotation
                boolean isNullable = field.isAnnotationPresent(Nullable.class);
                field.setAccessible(true);
                Object newValue = field.get(dto);
                if (newValue == null || (newValue.toString().isEmpty())) {
                    errorList.add(field.getName() + " cannot be empty");
                }
            }
            if(addressToupdate!=null) {
                if(dto.getState()!=null && !dto.getState().isEmpty())
                    addressToupdate.setState(districtService.findStateById(Integer.parseInt(dto.getState())));
                if(dto.getDistrict()!=null && !dto.getDistrict().isEmpty())
                    addressToupdate.setDistrict(districtService.findDistrictById(Integer.parseInt(dto.getDistrict())));
                if(dto.getAddress_line()!=null&& !dto.getAddress_line().isEmpty())
                    addressToupdate.setAddress_line(dto.getAddress_line());
                /*if(dto.getCity()!=null && !dto.getCity().isEmpty())
                    addressToupdate.setCity(dto.getCity());*/
                if(dto.getPincode()!=null && !dto.getPincode().isEmpty())
                    addressToupdate.setPincode(dto.getPincode());
                existingServiceProvider.setSpAddresses(addresses);
                serviceProviderAddress.setServiceProviderEntity(existingServiceProvider);
            }
            return errorList;
        }
    public static List<Long> getLongList(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;

            List<Long> longList = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Long) {
                    longList.add((Long) item);
                } else if (item instanceof Integer) {
                    longList.add(((Integer) item).longValue());
                }
            }

            return longList;
        }

        return Collections.emptyList();
    }

    @Transactional

    public Object searchServiceProviderBasedOnGivenFields(String state,String district,String first_name,String last_name,String mobileNumber) {
        Map<String, Character> alias = new HashMap<>();
        alias.put("state", 'a');
        alias.put("district", 'a');
        alias.put("first_name", 's');
        alias.put("last_name", 's');
        String generalizedQuery = "SELECT s.*\n" +
                "FROM service_provider s\n" +
                "JOIN custom_service_provider_address a ON s.service_provider_id = a.service_provider_id\n" +
                "WHERE ";
        if(mobileNumber!=null)
        {
            return entityManager.createQuery(Constant.PHONE_QUERY_SERVICE_PROVIDER, ServiceProviderEntity.class)
                    .setParameter("mobileNumber", mobileNumber)
                    .setParameter("country_code","+91")
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
        String[] fieldsNames = {"state", "district", "first_name","last_name"};
        String[] fields = {state, district, first_name,last_name};
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] != null) {
                generalizedQuery = generalizedQuery + alias.get(fieldsNames[i]) + "." + fieldsNames[i] + " =:" + fieldsNames[i] + " AND ";
            }
        }
        generalizedQuery = generalizedQuery.trim();
        int lastSpaceIndex = generalizedQuery.lastIndexOf(" ");
        generalizedQuery = generalizedQuery.substring(0, lastSpaceIndex);
        Query query;
        query = entityManager.createNativeQuery(generalizedQuery, ServiceProviderEntity.class);
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] != null)
                query.setParameter(fieldsNames[i], fields[i]);
        }
        List<ServiceProviderEntity>listOfSp= query.getResultList();
        List<Map<String,Object>>response=new ArrayList<>();
        for(ServiceProviderEntity serviceProvider:listOfSp)
        {
           response.add(sharedUtilityService.serviceProviderDetailsMap(serviceProvider));
        }
        return response;
    }
}