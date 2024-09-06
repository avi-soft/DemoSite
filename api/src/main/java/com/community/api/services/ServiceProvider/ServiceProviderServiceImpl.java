package com.community.api.services.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.endpoint.avisoft.controller.otpmodule.OtpEndpoint;
import com.community.api.entity.*;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.services.*;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import io.github.bucket4j.Bucket;
import org.apache.zookeeper.server.SessionTracker;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.social.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

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
    private  TwilioServiceForServiceProvider twilioService;
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
    private  RateLimiterService rateLimiterService;

    @Value("${twilio.phoneNumber}")
    private String twilioPhoneNumber;
    @Override
    @Transactional
    public ServiceProviderEntity saveServiceProvider(ServiceProviderEntity serviceProviderEntity) {
        try {
            entityManager.persist(serviceProviderEntity);
            ServiceProviderStatus serviceProviderStatus=entityManager.find(ServiceProviderStatus.class,1);
            serviceProviderEntity.setStatus(serviceProviderStatus);
            return serviceProviderEntity;
        } catch (Exception e) {
            throw new RuntimeException("Error saving service provider entity", e);
        }
    }




    @Transactional
    public ResponseEntity<?> updateServiceProvider(Long userId,Map<String, Object> updates) throws Exception {
        // Find existing ServiceProviderEntity
        ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, userId);
        if (existingServiceProvider == null) {
            throw new Exception("ServiceProvider with ID " + userId + " not found");
        }

        // Validate and check for unique constraints
        ServiceProviderEntity existingSPByUsername = null;
        ServiceProviderEntity existingSPByEmail = null;

        if (updates.containsKey("user_name")) {
            String userName = (String) updates.get("user_name");
            existingSPByUsername = findServiceProviderByUserName(userName);
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
        List<Skill>serviceProviderSkills=new ArrayList<>();
        List<ServiceProviderInfra>serviceProviderInfras=new ArrayList<>();
        List<ServiceProviderLanguage>serviceProviderLanguages=new ArrayList<>();
        List<Integer>infraList=getIntegerList(updates,"infra_list");
        List<Integer>skillList=getIntegerList(updates,"skill_list");
        List<Integer>languageList=getIntegerList(updates,"language_list");
        if(updates.containsKey("has_technical_knowledge")) {
            if ((boolean) updates.get("has_technical_knowledge").equals(true)) {
                if(!skillList.isEmpty()) {
                    for (int skill_id : skillList) {
                        Skill skill = entityManager.find(Skill.class, skill_id);
                        if (skill != null) {
                            if (!serviceProviderSkills.contains(skill))
                                serviceProviderSkills.add(skill);
                        }
                    }
                }
            }
        }else
            existingServiceProvider.setSkills(null);
        if(!infraList.isEmpty()) {
            for (int infra_id : infraList) {
                ServiceProviderInfra serviceProviderInfrastructure = entityManager.find(ServiceProviderInfra.class, infra_id);
                if (serviceProviderInfrastructure != null) {
                    if (!serviceProviderInfras.contains(serviceProviderInfrastructure))
                        serviceProviderInfras.add(serviceProviderInfrastructure);
                }
            }
        }
        if(!languageList.isEmpty()) {
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
        if(updates.containsKey("district")&&updates.containsKey("state"))
        {
            ServiceProviderAddress serviceProviderAddress=new ServiceProviderAddress();
            serviceProviderAddress.setAddress_type_id(findAddressName("CURRENT_ADDRESS").getAddress_type_Id());
            serviceProviderAddress.setPincode((String)updates.get("pincode"));
            serviceProviderAddress.setDistrict((String)updates.get("district"));
            serviceProviderAddress.setState((String)updates.get("state"));
            serviceProviderAddress.setCity((String)updates.get("city"));
            serviceProviderAddress.setAddress_line((String)updates.get("residential_address"));
            if(serviceProviderAddress.getAddress_line()!=null||serviceProviderAddress.getCity()!=null||serviceProviderAddress.getDistrict()!=null||serviceProviderAddress.getState()!=null||serviceProviderAddress.getPincode()!=null) {
                addAddress(existingServiceProvider.getService_provider_id(), serviceProviderAddress);
            }
        }
        //removing key for address
            updates.remove("address_line");
            updates.remove("city");
            updates.remove("state");
            updates.remove("district");
            updates.remove("pincode");

        // Update only the fields that are present in the map using reflections
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String fieldName = entry.getKey();
            Object newValue = entry.getValue();

            try {
                Field field = ServiceProviderEntity.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                // Optionally, check for type compatibility before setting the value
                if (newValue != null && field.getType().isAssignableFrom(newValue.getClass())) {

                    field.set(existingServiceProvider, newValue);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Handle the exception if the field is not found or not accessible
                return responseService.generateErrorResponse("Invalid field: " + fieldName, HttpStatus.BAD_REQUEST);
            }
        }
        // Merge the updated entity
        entityManager.merge(existingServiceProvider);
        if(existingServiceProvider.getUser_name()==null) {
            String username=generateUsernameForServiceProvider(existingServiceProvider);
            existingServiceProvider.setUser_name(username);
        }
        entityManager.merge(existingServiceProvider);
        return responseService.generateSuccessResponse("Service Provider Updated Successfully",existingServiceProvider,HttpStatus.OK);
    }

    @Override
    public ServiceProviderEntity getServiceProviderById(Long userId) {
        return entityManager.find(ServiceProviderEntity.class, userId);
    }
    @Transactional
    public ResponseEntity<?> sendOtpToMobile(String mobileNumber,String countryCode) {

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


            ServiceProviderEntity existingServiceProvider=findServiceProviderByPhone(mobileNumber,countryCode);
            if(existingServiceProvider == null){
                ServiceProviderEntity serviceProviderEntity=new ServiceProviderEntity();
                serviceProviderEntity.setService_provider_id(customerService.findNextCustomerId());
                serviceProviderEntity.setCountry_code(Constant.COUNTRY_CODE);
                serviceProviderEntity.setMobileNumber(mobileNumber);
               serviceProviderEntity.setOtp(otp);
                entityManager.persist(serviceProviderEntity);

            }else{
                existingServiceProvider.setOtp(otp);
                entityManager.merge(existingServiceProvider);
            }


            return responseService.generateSuccessResponse("OTP has been sent successfully !!!" ,otp,HttpStatus.OK);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return responseService.generateErrorResponse("Unauthorized access: Please check your API key",HttpStatus.UNAUTHORIZED);
            } else {
                exceptionHandling.handleHttpClientErrorException(e);
                return responseService.generateErrorResponse("Internal server error occurred",HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return responseService.generateErrorResponse("Error sending OTP: " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error sending OTP: " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    public synchronized String generateOTP() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(8999);
        return String.valueOf(otp);
    }


    @Transactional
    public boolean setotp(String mobileNumber,String countryCode) {
        ServiceProviderEntity exisitingServiceProvider=findServiceProviderByPhone(mobileNumber,countryCode);

        if(exisitingServiceProvider!=null){
            String storedOtp = exisitingServiceProvider.getOtp();
            if(storedOtp!=null){
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
        return Pattern.compile(mobileNumberPattern).matcher(mobileNumber).matches();
    }

    public ServiceProviderEntity findServiceProviderByPhone(String mobileNumber,String countryCode) {

        return entityManager.createQuery(Constant.PHONE_QUERY_SERVICE_PROVIDER, ServiceProviderEntity.class)
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("country_code",countryCode)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public ServiceProviderEntity findServiceProviderByUserName(String username) {

        return entityManager.createQuery(Constant.USERNAME_QUERY_SERVICE_PROVIDER, ServiceProviderEntity.class)
                .setParameter("username",username)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
    //find service provider by phone and validate the password.
    public ResponseEntity<?> authenticateByPhone(String mobileNumber, String countryCode, String password,HttpServletRequest request,HttpSession session) {
        ServiceProviderEntity existingServiceProvider = findServiceProviderByPhone(mobileNumber, countryCode);
        return validateServiceProvider(existingServiceProvider, password,request,session);
    }
    //find service provider by username and validate the password.
    public ResponseEntity<?> authenticateByUsername(String username, String password,HttpServletRequest request,HttpSession session) {
        ServiceProviderEntity existingServiceProvider =findServiceProviderByUserName(username);
        return validateServiceProvider(existingServiceProvider, password,request,session);
    }
    //mechanism to check password
    public ResponseEntity<?> validateServiceProvider(ServiceProviderEntity serviceProvider, String password, HttpServletRequest request,HttpSession session) {
        if (serviceProvider == null) {
            return responseService.generateErrorResponse("No Records Found", HttpStatus.NOT_FOUND);
        }
        if (passwordEncoder.matches(password,serviceProvider.getPassword())) {
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String tokenKey = "authTokenServiceProvider_" + serviceProvider.getMobileNumber();
            String existingToken = (String) session.getAttribute(tokenKey);
            if(existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {
                Map<String, Object> responseBody = createAuthResponse(existingToken, serviceProvider).getBody();

                return ResponseEntity.ok(responseBody);
            } else {
                String newToken = jwtUtil.generateToken(serviceProvider.getService_provider_id(), serviceProvider.getRole(), ipAddress, userAgent);
                session.setAttribute(tokenKey, newToken);

                Map<String, Object> responseBody = createAuthResponse(newToken, serviceProvider).getBody();
            return ResponseEntity.ok(responseBody);

        } }else {
            return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
        }
    }
    public ResponseEntity<?> loginWithPassword(@RequestBody Map<String, Object> serviceProviderDetails,HttpServletRequest request,HttpSession session) {
        try {
            String mobileNumber = (String) serviceProviderDetails.get("mobileNumber");
            if(mobileNumber.startsWith("0"))
                mobileNumber=mobileNumber.substring(1);
            String username = (String) serviceProviderDetails.get("username");
            String password = (String) serviceProviderDetails.get("password");
            String countryCode = (String) serviceProviderDetails.getOrDefault("countryCode", Constant.COUNTRY_CODE);
            // Check for empty password
            if (password == null || password.isEmpty()) {
                return responseService.generateErrorResponse("Password cannot be empty", HttpStatus.BAD_REQUEST);

            }
            if (mobileNumber != null && !mobileNumber.isEmpty()) {
                return authenticateByPhone(mobileNumber, countryCode, password,request,session);
            } else if (username != null && !username.isEmpty()) {
                return authenticateByUsername(username, password,request,session);
            } else {
                return responseService.generateErrorResponse("Empty Phone Number or username", HttpStatus.BAD_REQUEST);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    public ResponseEntity<?>loginWithUsernameAndOTP(String username,HttpSession session)
    {
        try {
            if (username == null ) {
                return responseService.generateErrorResponse(ApiConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);

            }
            ServiceProviderEntity existingServiceProivder = findServiceProviderByUserName(username);
            if (existingServiceProivder == null){
                return responseService.generateErrorResponse("No records found", HttpStatus.NOT_FOUND);


            }
            if (existingServiceProivder.getMobileNumber() == null) {
                return responseService.generateErrorResponse("No mobile Number registerd for this account", HttpStatus.NOT_FOUND);

            }
            String countryCode=existingServiceProivder.getCountry_code();
            if(countryCode==null)
                countryCode=Constant.COUNTRY_CODE;
            return (sendOtp(existingServiceProivder.getMobileNumber(),countryCode,session));
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    public ResponseEntity<?> sendOtp(String mobileNumber, String countryCode, HttpSession session) throws UnsupportedEncodingException {
        try {
            mobileNumber = mobileNumber.startsWith("0")
                    ? mobileNumber.substring(1)
                    : mobileNumber;
            if(countryCode==null)
                countryCode=Constant.COUNTRY_CODE;
            Bucket bucket = rateLimiterService.resolveBucket(mobileNumber, "/service-provider/otp/send-otp");
            if (bucket.tryConsume(1)) {
                if (!isValidMobileNumber(mobileNumber)) {
                    return responseService.generateErrorResponse("Invalid mobile number", HttpStatus.BAD_REQUEST);

                }
                ResponseEntity<?> otpResponse = twilioService.sendOtpToMobile(mobileNumber,countryCode);
                return otpResponse;
            } else {
                return responseService.generateErrorResponse("You can send OTP only once in 1 minute", HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);

            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse(ApiConstants.SOME_EXCEPTION_OCCURRED + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    public String generateUsernameForServiceProvider(ServiceProviderEntity serviceProviderDetails)
    {
        String firstName = serviceProviderDetails.getFirst_name();
        String lastName = serviceProviderDetails.getLast_name();
        String state = serviceProviderDetails.getSpAddresses().get(0).getState();
        String username=null;
        StateCode stateDetails;
        if (firstName != null && lastName != null && state != null)
        {
            stateDetails=findStateCode(state);
            username=stateDetails.getState_code()+firstName+lastName;
            //suffix check
            //if a user already exist with username like PBRajSharma
            if(!findServiceProviderListByUsername(username).isEmpty())
            {
                List<ServiceProviderEntity>listOfSp=findServiceProviderListByUsername(username);
                ServiceProviderEntity serviceProvider=listOfSp.get(listOfSp.size()-1);
                String suffix=serviceProvider.getUser_name().substring(serviceProvider.getUser_name().length()-2);
                int suffixValue=Integer.parseInt(suffix);
                if(suffixValue<9)
                    username=username+"0"+Integer.toString(suffixValue+1);
                else
                    username=username+Integer.toString(suffixValue+1);
            }
            //simply adding 01 if there are no users for the given username
            else
                username=username+"01";
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
            Integer role=(Integer) serviceProviderDetails.get("role");
            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }

            if (username != null) {
                ServiceProviderEntity serviceProvider = findServiceProviderByUserName(username);
                if (serviceProvider == null) {
                    return responseService.generateErrorResponse("No records found ",HttpStatus.NOT_FOUND);

                }
                mobileNumber = serviceProvider.getMobileNumber();
            } else if (mobileNumber == null || mobileNumber.isEmpty()) {
                return responseService.generateErrorResponse("mobile number can not be null ",HttpStatus.BAD_REQUEST);

            }

            if (!isValidMobileNumber(mobileNumber)) {
                return responseService.generateErrorResponse("Invalid mobile number ",HttpStatus.BAD_REQUEST);

            }
            if(mobileNumber.startsWith("0"))
                mobileNumber= mobileNumber.substring(1);
            ServiceProviderEntity existingServiceProvider = findServiceProviderByPhone(mobileNumber, countryCode);

            if(existingServiceProvider == null){
                return responseService.generateErrorResponse("Invalid Data Provided ",HttpStatus.UNAUTHORIZED);

            }

            String storedOtp =  existingServiceProvider.getOtp();
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String tokenKey = "authTokenServiceProvider_" + mobileNumber;


            if (otpEntered == null || otpEntered.trim().isEmpty()) {
                return responseService.generateErrorResponse("OTP cannot be empty",HttpStatus.BAD_REQUEST);
            }
            if (otpEntered.equals(storedOtp)) {
                existingServiceProvider.setOtp(null);
                entityManager.merge(existingServiceProvider);

                String existingToken = (String) session.getAttribute(tokenKey);

                if (existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {
                    Map<String, Object> responseBody = createAuthResponse(existingToken, existingServiceProvider).getBody();

                    return ResponseEntity.ok(responseBody);
                } else {
                    String newToken = jwtUtil.generateToken(existingServiceProvider.getService_provider_id(), role, ipAddress, userAgent);
                    session.setAttribute(tokenKey, newToken);

                    Map<String, Object> responseBody = createAuthResponse(newToken, existingServiceProvider).getBody();
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

    private ResponseEntity<Map<String, Object>> createAuthResponse(String token, ServiceProviderEntity serviceProviderEntity) {
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
                .setParameter("state_name",state_name)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
    public ServiceProviderEntity findSPbyEmail(String email) {

        return entityManager.createQuery(Constant.SP_EMAIL_QUERY, ServiceProviderEntity.class)
                .setParameter("email",email)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
    public ServiceProviderAddressRef findAddressName(String address_name) {

        return entityManager.createQuery(Constant.GET_SERVICE_PROVIDER_DEFAULT_ADDRESS, ServiceProviderAddressRef.class)
                .setParameter("address_name",address_name)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
    public List<ServiceProviderEntity> findServiceProviderListByUsername(String username) {
        username=username+"%";
        return entityManager.createQuery(Constant.SP_USERNAME_QUERY, ServiceProviderEntity.class)
                .setParameter("username",username)
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
    public ResponseEntity<?> addAddress(long serviceProviderId,ServiceProviderAddress serviceProviderAddress) throws Exception {
        try{
            if(serviceProviderAddress==null)
            {
                return responseService.generateErrorResponse("Incomplete Details",HttpStatus.BAD_REQUEST);
            }
            ServiceProviderEntity existingServiceProvider=entityManager.find(ServiceProviderEntity.class,serviceProviderId);
            if(existingServiceProvider==null)
            {
                return responseService.generateErrorResponse("Service Provider Not found",HttpStatus.BAD_REQUEST);
            }
            List<ServiceProviderAddress>addresses=existingServiceProvider.getSpAddresses();
            serviceProviderAddress.setState(districtService.findStateById(Integer.parseInt(serviceProviderAddress.getState())));
            serviceProviderAddress.setDistrict(districtService.findDistrictById(Integer.parseInt(serviceProviderAddress.getDistrict())));
            addresses.add(serviceProviderAddress);
            existingServiceProvider.setSpAddresses(addresses);
            serviceProviderAddress.setServiceProviderEntity(existingServiceProvider);

            entityManager.persist(serviceProviderAddress);

            entityManager.merge(existingServiceProvider);
            return responseService.generateSuccessResponse("Address added successfully",serviceProviderAddress,HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error adding address",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}