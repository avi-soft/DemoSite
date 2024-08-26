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
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
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
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TwilioServiceForServiceProvider twilioService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private DistrictService districtService;
    /*@PostMapping
    public ResponseEntity<ServiceProviderEntity> createServiceProvider(@RequestBody ServiceProviderEntity serviceProviderEntity) throws Exception {
        ServiceProviderEntity savedServiceProvider = serviceProviderService.saveServiceProvider(serviceProviderEntity);

        if (savedServiceProvider == null) {
            throw new Exception("Service provider could not be created");
        }
        return ResponseEntity.ok(savedServiceProvider);
    }*/
    @Transactional
    @PostMapping("/assign-skill")
    public ResponseEntity<?>addSkill(@RequestParam Long serviceProviderId,@RequestParam Long skillId)
    {
        try {
            Skill skill = entityManager.find(Skill.class, skillId);
            ServiceProviderEntity serviceProviderEntity = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            List<Skill> listOfSkills = serviceProviderEntity.getSkills();
            listOfSkills.add(skill);
            serviceProviderEntity.setSkills(listOfSkills);
            entityManager.merge(serviceProviderEntity);
            return new ResponseEntity<>(serviceProviderEntity, HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error assigning skill: " + e.getMessage());
        }
    }
    @PatchMapping("update")
    public ResponseEntity<?> updateServiceProvider(@RequestParam Long userId, @RequestBody ServiceProviderEntity serviceProviderDetails) throws Exception {
        try{
        return serviceProviderService.updateServiceProvider(userId,serviceProviderDetails);
    }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some updating: " + e.getMessage());
        }
    }
    @DeleteMapping("delete")
    public ResponseEntity<?>deleteServiceProvider(@RequestParam Long userId)
    {
        try{
        ServiceProviderEntity serviceProviderToBeDeleted=entityManager.find(ServiceProviderEntity.class,userId);
        if(serviceProviderToBeDeleted==null)
            return new ResponseEntity<>("No record found",HttpStatus.NOT_FOUND);
        else
            entityManager.remove(serviceProviderToBeDeleted);
        return new ResponseEntity<>("Service Provider Deleted",HttpStatus.OK);
    }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting: " + e.getMessage());
        }}
    @Transactional
    @PostMapping("createOrUpdatePassword")
    public ResponseEntity<?>deleteServiceProvider(@RequestBody Map<String,Object>passwordDetails,@RequestParam long userId)
    {
        try {
            String password = (String) passwordDetails.get("password");
            String newPassword = (String) passwordDetails.get("newPassword");
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, userId);
            if (serviceProvider == null)
                return new ResponseEntity<>("No records found", HttpStatus.NOT_FOUND);
            if (serviceProvider.getPassword() == null) {
                serviceProvider.setPassword(passwordEncoder.encode(password));
                entityManager.merge(serviceProvider);
                return new ResponseEntity<>("Password created", HttpStatus.OK);
            } else {
                if (password == null || newPassword == null)
                    new ResponseEntity<>("Empty password", HttpStatus.BAD_REQUEST);
                if (passwordEncoder.matches(password, serviceProvider.getPassword())) {
                    serviceProvider.setPassword(passwordEncoder.encode(newPassword));
                    return new ResponseEntity<>("New Password Set", HttpStatus.OK);
                } else
                    return new ResponseEntity<>("Password do not match", HttpStatus.BAD_REQUEST);
            }
        }
        catch (Exception e) {
                exceptionHandling.handleException(e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error changing/updating password: " + e.getMessage());
            }
    }
    @GetMapping("getServiceProivider")
    public ResponseEntity<?> getServiceProviderById(@RequestParam Long userId) throws Exception {
        try {
            ServiceProviderEntity serviceProviderEntity = serviceProviderService.getServiceProviderById(userId);
            if (serviceProviderEntity == null) {
                throw new Exception("ServiceProvider with ID " + userId + " not found");
            }
            return ResponseEntity.ok(serviceProviderEntity);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some fetching account " + e.getMessage());
        }
    }
    @Transactional
    @PostMapping("/addAddress")
    public ResponseEntity<?> addAddress(@RequestParam long serviceProviderId,@RequestBody ServiceProviderAddress serviceProviderAddress) throws Exception {
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
            if(existingServiceProvider.getUser_name()==null) {
                String username=serviceProviderService.generateUsernameForServiceProvider(existingServiceProvider);
                System.out.println(existingServiceProvider.toString());
                existingServiceProvider.setUser_name(username);
            }
            entityManager.persist(serviceProviderAddress);

            entityManager.merge(existingServiceProvider);
            return new ResponseEntity<>(serviceProviderAddress,HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error adding address " + e.getMessage());
        }
    }
    @GetMapping("/getAddressNames")
    public ResponseEntity<?> getAddressTypes()
    {
        try{
        TypedQuery<ServiceProviderAddressRef> query = entityManager.createQuery(Constant.jpql, ServiceProviderAddressRef.class);
        return new ResponseEntity<>(query.getResultList(),HttpStatus.OK);
    }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some issue in fetching addressNames " + e.getMessage());
        }
    }
}