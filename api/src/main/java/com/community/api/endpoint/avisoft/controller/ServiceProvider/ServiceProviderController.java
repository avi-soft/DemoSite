package com.community.api.endpoint.avisoft.controller.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.ServiceProviderAddress;
import com.community.api.entity.ServiceProviderAddressRef;
import com.community.api.entity.Skill;
import com.community.api.services.DistrictService;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.TwilioServiceForServiceProvider;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpSession;
import javax.swing.text.html.parser.Entity;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/service-providers")
public class ServiceProviderController {

    @Autowired
    private ServiceProviderServiceImpl serviceProviderService;
    @Autowired
    private EntityManager entityManager;
    @Value("${twilio.accountSid}")
    private String accountSid;
    @Value("${twilio.authToken}")
    private String authToken;
    @Autowired
    private TwilioServiceForServiceProvider twilioService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private DistrictService districtService;
    @PostMapping
    public ResponseEntity<ServiceProviderEntity> createServiceProvider(@RequestBody ServiceProviderEntity serviceProviderEntity) throws Exception {
        ServiceProviderEntity savedServiceProvider = serviceProviderService.saveServiceProvider(serviceProviderEntity);

        if (savedServiceProvider == null) {
            throw new Exception("Service provider could not be created");
        }
        return ResponseEntity.ok(savedServiceProvider);
    }
    @Transactional
    @PostMapping("/add-skill/{serviceProviderId}/{skillId}")
    public ResponseEntity<?>addSkill(@PathVariable Long serviceProviderId,@PathVariable Long skillId)
    {
        Skill skill=entityManager.find(Skill.class,skillId);
        ServiceProviderEntity serviceProviderEntity=entityManager.find(ServiceProviderEntity.class,serviceProviderId);
        List<Skill> listOfSkills=serviceProviderEntity.getSkills();
        listOfSkills.add(skill);
        serviceProviderEntity.setSkills(listOfSkills);
        entityManager.merge(serviceProviderEntity);
        return new ResponseEntity<>(serviceProviderEntity, HttpStatus.OK);
    }
    @PatchMapping("update")
    public ResponseEntity<?> updateServiceProvider(@RequestBody Long userId, @RequestBody ServiceProviderEntity serviceProviderDetails) throws Exception {
        ServiceProviderEntity updatedServiceProvider = serviceProviderService.updateServiceProvider(userId,serviceProviderDetails);
        return ResponseEntity.ok(updatedServiceProvider);
    }
    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<String> sendOtpToMobile(@RequestBody Map<String, Object> signupDetails) {
        try {
            String mobileNumber = (String) signupDetails.get("mobileNumber");
            String countryCode = (String) signupDetails.get("countryCode");

            if (mobileNumber == null || mobileNumber.isEmpty()) {
                throw new IllegalArgumentException("Mobile number cannot be null or empty");
            }

            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }

            Twilio.init(accountSid, authToken);
            String completeMobileNumber = countryCode + mobileNumber;
            String otp = serviceProviderService.generateOTP();

            ServiceProviderEntity existingServiceProvider = serviceProviderService.findServiceProviderByPhone(mobileNumber, countryCode);

            if (existingServiceProvider == null) {
                // New entity, use persist
                ServiceProviderEntity serviceProviderEntity = new ServiceProviderEntity();
                serviceProviderEntity.setCountry_code(countryCode); // Make sure to use the provided or default country code
                serviceProviderEntity.setMobileNumber(mobileNumber);
                serviceProviderEntity.setOtp(otp);
                entityManager.persist(serviceProviderEntity);
            } else {
                // Existing entity, use merge
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
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
        }
    }
    @GetMapping("getServiceProivider")
    public ResponseEntity<ServiceProviderEntity> getServiceProviderById(@RequestParam Long userId) throws Exception {
        ServiceProviderEntity serviceProviderEntity = serviceProviderService.getServiceProviderById(userId);
        if (serviceProviderEntity == null) {
            throw new Exception("ServiceProvider with ID " + userId + " not found");
        }
        return ResponseEntity.ok(serviceProviderEntity);
    }
    @Transactional
    @PostMapping("/addAddress")
    public ResponseEntity<?> addAddress(@RequestParam long serviceProviderId,@RequestParam int district_id,@RequestParam int state_id,@RequestBody ServiceProviderAddress serviceProviderAddress) throws Exception {
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
            serviceProviderAddress.setState(districtService.findStateById(state_id));
            serviceProviderAddress.setDistrict(districtService.findDistrictById(district_id));
            addresses.add(serviceProviderAddress);
            existingServiceProvider.setSpAddresses(addresses);
            serviceProviderAddress.setServiceProviderEntity(existingServiceProvider);

            entityManager.persist(serviceProviderAddress);
            if(existingServiceProvider.getSpAddresses().size()==1) { //generate the username ,the moment first address is registered.
                if (existingServiceProvider.getUser_name() == null)
                    existingServiceProvider.setUser_name(serviceProviderService.generateUsernameForServiceProvider(existingServiceProvider));
            }
            entityManager.merge(existingServiceProvider);
            return new ResponseEntity<>(serviceProviderAddress,HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error sending OTP: " + e.getMessage());
        }
    }
    @GetMapping("/getAddressTypes")
    public ResponseEntity<?> getAddressTypes()
    {
        TypedQuery<ServiceProviderAddressRef> query = entityManager.createQuery(Constant.jpql, ServiceProviderAddressRef.class);
        return new ResponseEntity<>(query.getResultList(),HttpStatus.OK);
    }
}