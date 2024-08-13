package com.community.api.services.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.entity.CustomCustomer;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.endpoint.serviceProvider.ServiceProviderStatus;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.social.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.Random;
import java.util.regex.Pattern;

@Service
public class ServiceProviderServiceImpl implements ServiceProviderService {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private CustomerService customerService;
    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

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
    public ServiceProviderEntity updateServiceProvider(Long userId, @RequestBody ServiceProviderEntity serviceProviderEntity) throws Exception {
        ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, userId);
        if (existingServiceProvider == null) {
            throw new Exception("ServiceProvider with ID " + userId + " not found");
        }

        serviceProviderEntity.setPrimary_mobile_number(existingServiceProvider.getPrimary_mobile_number());
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
        return existingServiceProvider;
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
                serviceProviderEntity.setUser_ID(customerService.findNextCustomerId());
                serviceProviderEntity.setCountry_code(Constant.COUNTRY_CODE);
                serviceProviderEntity.setPrimary_mobile_number(mobileNumber);
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

    private synchronized String generateOTP() {
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
                .setParameter("primaryMobileNumber", mobileNumber)
                .setParameter("countryCode",countryCode)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public ServiceProviderEntity findServiceProviderByUserName(String username) {

        return entityManager.createQuery(Constant.PHONE_QUERY_SERVICE_PROVIDER, ServiceProviderEntity.class)
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
}