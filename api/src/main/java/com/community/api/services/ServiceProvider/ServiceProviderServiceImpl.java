package com.community.api.services.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.entity.CustomCustomer;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.entity.StateCode;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.RateLimiterService;
import com.community.api.services.TwilioServiceForServiceProvider;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

    @Override
    @Transactional
    public ResponseEntity<?> updateServiceProvider(Long userId, @RequestBody ServiceProviderEntity serviceProviderEntity) throws Exception {
        ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, userId);
        if (existingServiceProvider == null) {
            throw new Exception("ServiceProvider with ID " + userId + " not found");
        }
        serviceProviderEntity.setMobileNumber(existingServiceProvider.getMobileNumber());
        ServiceProviderEntity existingSPByUsername = null;
        ServiceProviderEntity existingSPByEmail = null;
        if (serviceProviderEntity.getUser_name() != null) {
            existingSPByUsername = findServiceProviderByUserName(serviceProviderEntity.getUser_name());
        }
        if (serviceProviderEntity.getPrimary_email() != null) {
            existingSPByEmail = findSPbyEmail(serviceProviderEntity.getPrimary_email());
        }
        if ((existingSPByUsername != null) || existingSPByEmail != null) {
            if (existingSPByUsername != null && !existingSPByUsername.getService_provider_id().equals(userId)) {
                return new ResponseEntity<>("Username is not available", HttpStatus.BAD_REQUEST);
            }
                if (existingSPByEmail != null && !existingSPByEmail.getService_provider_id().equals(userId)) {
                return new ResponseEntity<>("Email not available", HttpStatus.BAD_REQUEST);
            }
        }
        for(Field field:ServiceProviderEntity.class.getDeclaredFields())
        {
            field.setAccessible(true);
            Object newValue=field.get(serviceProviderEntity);
            if(newValue!=null)
            {
                field.set(existingServiceProvider,newValue);
            }
        }


        entityManager.merge(existingServiceProvider);
        return new ResponseEntity<>(serviceProviderEntity,HttpStatus.OK);
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
        if (serviceProvider.getPassword().equals(password)) {
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

            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE; // Default value if not provided
            }

            if (username != null) {
                ServiceProviderEntity serviceProvider = findServiceProviderByUserName(username);
                if (serviceProvider == null) {
                    return new ResponseEntity<>("No records found", HttpStatus.NOT_FOUND);
                }
                mobileNumber = serviceProvider.getMobileNumber(); // Get the mobile number from the service provider
            } else if (mobileNumber == null || mobileNumber.isEmpty()) {
                return new ResponseEntity<>("Empty Credentials", HttpStatus.BAD_REQUEST);
            }

            if (!isValidMobileNumber(mobileNumber)) {
                return new ResponseEntity<>("Invalid mobile number", HttpStatus.BAD_REQUEST);
            }

            ServiceProviderEntity existingServiceProvider = findServiceProviderByPhone(mobileNumber, countryCode);
            String storedOtp =  existingServiceProvider.getOtp();



            if (otpEntered == null || otpEntered.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("OTP cannot be empty");
            }
            if (otpEntered.equals(storedOtp)) {
                existingServiceProvider.setOtp(null); // Clear the OTP after successful verification
                entityManager.merge(existingServiceProvider); // Persist the changes
                // Return the service provider entity or create and return JWT token as needed
                return ResponseEntity.ok(existingServiceProvider);
            } else {
                // Return a more informative error message if needed
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
            }

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error verifying OTP", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
    public List<ServiceProviderEntity> findServiceProviderListByUsername(String username) {
        username=username+"%";
        return entityManager.createQuery(Constant.SP_USERNAME_QUERY, ServiceProviderEntity.class)
                .setParameter("username",username)
                .getResultList();
    }

}