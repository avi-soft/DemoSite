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
    public ResponseEntity<String> sendOtpToMobile(String mobileNumber,String countryCode) {

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


            return ResponseEntity.ok("OTP has been sent successfully " + otp);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access: Please check your API key");
            } else {
                exceptionHandling.handleHttpClientErrorException(e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error occurred");
            }
        } catch (ApiException e) {
            exceptionHandling.handleApiException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
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
    public ResponseEntity<?> authenticateByPhone(String mobileNumber, String countryCode, String password) {
        ServiceProviderEntity existingServiceProvider = findServiceProviderByPhone(mobileNumber, countryCode);
        return validateServiceProvider(existingServiceProvider, password);
    }
    //find service provider by username and validate the password.
    public ResponseEntity<?> authenticateByUsername(String username, String password) {
        ServiceProviderEntity existingServiceProvider =findServiceProviderByUserName(username);
        return validateServiceProvider(existingServiceProvider, password);
    }
    //mechanism to check password
    public ResponseEntity<?> validateServiceProvider(ServiceProviderEntity serviceProvider, String password) {
        if (serviceProvider == null) {
            return new ResponseEntity<>("No Records Found", HttpStatus.NOT_FOUND);
        }
        if (passwordEncoder.matches(password,serviceProvider.getPassword())) {
            return new ResponseEntity<>(serviceProvider, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid Password", HttpStatus.UNAUTHORIZED);
        }
    }
    public ResponseEntity<?> loginWithPassword(@RequestBody Map<String, Object> serviceProviderDetails, HttpSession session) {
        try {
            String mobileNumber = (String) serviceProviderDetails.get("mobileNumber");
            String username = (String) serviceProviderDetails.get("username");
            String password = (String) serviceProviderDetails.get("password");
            String countryCode = (String) serviceProviderDetails.getOrDefault("countryCode", Constant.COUNTRY_CODE);
            // Check for empty password
            if (password == null || password.isEmpty()) {
                return new ResponseEntity<>("Password cannot be empty", HttpStatus.BAD_REQUEST);
            }
            if (mobileNumber != null && !mobileNumber.isEmpty()) {
                return authenticateByPhone(mobileNumber, countryCode, password);
            } else if (username != null && !username.isEmpty()) {
                return authenticateByUsername(username, password);
            } else {
                return new ResponseEntity<>("Empty Phone Number or username", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
    public ResponseEntity<?>loginWithUsernameAndOTP(String username,HttpSession session)
    {
        try {
            if (username == null ) {
                return new ResponseEntity<>("Empty Credentials", HttpStatus.BAD_REQUEST);
            }
            ServiceProviderEntity existingServiceProivder = findServiceProviderByUserName(username);
            if (existingServiceProivder == null)
                return new ResponseEntity<>("No records found", HttpStatus.NOT_FOUND);
            if (existingServiceProivder.getMobileNumber() == null) {
                return new ResponseEntity<>("No mobile Number registerd for this account", HttpStatus.NOT_FOUND);
            }
            String countryCode=existingServiceProivder.getCountry_code();
            if(countryCode==null)
                countryCode=Constant.COUNTRY_CODE;
            return new ResponseEntity<>(sendOtp(existingServiceProivder.getMobileNumber(),countryCode,session),HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
        }
    }
    public ResponseEntity<String> sendOtp(String mobileNumber, String countryCode, HttpSession session) throws UnsupportedEncodingException {
        try {
            mobileNumber = mobileNumber.startsWith("0")
                    ? mobileNumber.substring(1)
                    : mobileNumber;
            if(countryCode==null)
                countryCode=Constant.COUNTRY_CODE;
            Bucket bucket = rateLimiterService.resolveBucket(mobileNumber, "/service-provider/otp/send-otp");
            if (bucket.tryConsume(1)) {
                if (!isValidMobileNumber(mobileNumber)) {
                    return ResponseEntity.badRequest().body("Invalid mobile number");
                }
                ResponseEntity<String> otpResponse = twilioService.sendOtpToMobile(mobileNumber,countryCode);
                return otpResponse;
            } else {
                return ResponseEntity.ok("You can send OTP only once in 1 minute");
            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
        }
    }
//    todo:- need to test with same user details with atleast 10 users
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
                countryCode = Constant.COUNTRY_CODE; // Default value if not provided
            }

            if (username != null) {
                ServiceProviderEntity serviceProvider = findServiceProviderByUserName(username);
                if (serviceProvider == null) {
                    return responseService.generateErrorResponse("No records found ",HttpStatus.NOT_FOUND);

                }
                mobileNumber = serviceProvider.getMobileNumber(); // Get the mobile number from the service provider
            } else if (mobileNumber == null || mobileNumber.isEmpty()) {
                return responseService.generateErrorResponse("mobile number can not be null ",HttpStatus.BAD_REQUEST);

            }

            if (!isValidMobileNumber(mobileNumber)) {
                return responseService.generateErrorResponse("Invalid mobile number ",HttpStatus.BAD_REQUEST);

            }
            if(mobileNumber.startsWith("0"))
                mobileNumber= mobileNumber.substring(1);
            ServiceProviderEntity existingServiceProvider = findServiceProviderByPhone(mobileNumber, countryCode);
            String storedOtp =  existingServiceProvider.getOtp();
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String tokenKey = "authTokenServiceProvider_" + mobileNumber;


            if (otpEntered == null || otpEntered.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("OTP cannot be empty");
            }
            if (otpEntered.equals(storedOtp)) {
                existingServiceProvider.setOtp(null);
                entityManager.merge(existingServiceProvider);

                String existingToken = (String) session.getAttribute(tokenKey);

                if (existingToken != null && jwtUtil.validateToken(existingToken, ipAddress, userAgent)) {
                    return ResponseEntity.ok(createAuthResponse(existingToken, existingServiceProvider));
                } else {
                    String newToken = jwtUtil.generateToken(existingServiceProvider.getService_provider_id(), role, ipAddress, userAgent);
                    session.setAttribute(tokenKey, newToken);
                    return ResponseEntity.ok(createAuthResponse(newToken, existingServiceProvider));
                }
            } else {
                // Return a more informative error message if needed
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error verifying OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<?> createAuthResponse(String token, ServiceProviderEntity serviceProviderEntity) {

        AuthResponseServiceProvider authResponse = new AuthResponseServiceProvider(token, serviceProviderEntity);
        return responseService.generateSuccessResponse("Token details ",authResponse,HttpStatus.OK);

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

    private class AuthResponseServiceProvider {
        private String token;
        private ServiceProviderEntity serviceProviderDetails;

        public AuthResponseServiceProvider(String token, ServiceProviderEntity serviceProviderDetails) {
            this.token = token;
            this.serviceProviderDetails = serviceProviderDetails;
        }

        public String getToken() {
            return token;
        }

        public ServiceProviderEntity getUserDetails() {
            return serviceProviderDetails;
        }
    }
    @SuppressWarnings("unchecked")
    public static List<Integer> getIntegerList(Map<String, Object> map, String key) {
        // Retrieve the object associated with the key
        Object value = map.get(key);

        // Check if the value is an instance of List
        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;

            // Check if the list is not empty and the first element is Integer
            if (!list.isEmpty() && list.get(0) instanceof Integer) {
                // Safe to cast the list to List<Integer>
                return (List<Integer>) list;
            }
        }

        // Return an empty list if the conditions are not met
        return Collections.emptyList();
    }
    @Transactional
    public ResponseEntity<?> addAddress(long serviceProviderId,ServiceProviderAddress serviceProviderAddress) throws Exception {
        try{
            if(serviceProviderAddress==null)
            {
                return new ResponseEntity<>("Incomplete Details",HttpStatus.BAD_REQUEST);
            }
            ServiceProviderEntity existingServiceProvider=entityManager.find(ServiceProviderEntity.class,serviceProviderId);
            if(existingServiceProvider==null)
            {
                return new ResponseEntity<>("Service Provider Not found",HttpStatus.BAD_REQUEST);
            }
            List<ServiceProviderAddress>addresses=existingServiceProvider.getSpAddresses();
            serviceProviderAddress.setState(districtService.findStateById(Integer.parseInt(serviceProviderAddress.getState())));
            serviceProviderAddress.setDistrict(districtService.findDistrictById(Integer.parseInt(serviceProviderAddress.getDistrict())));
            addresses.add(serviceProviderAddress);
            existingServiceProvider.setSpAddresses(addresses);
            serviceProviderAddress.setServiceProviderEntity(existingServiceProvider);

            entityManager.persist(serviceProviderAddress);

            entityManager.merge(existingServiceProvider);
            return new ResponseEntity<>(serviceProviderAddress,HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error adding address " + e.getMessage());
        }
    }
}