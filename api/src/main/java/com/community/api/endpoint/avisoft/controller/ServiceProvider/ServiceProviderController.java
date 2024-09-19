package com.community.api.endpoint.avisoft.controller.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.dto.UpdateTestStatusRank;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.ServiceProviderTestStatus;
import com.community.api.services.DistrictService;
import com.community.api.services.ResponseService;
import com.community.api.entity.ServiceProviderAddress;
import com.community.api.entity.ServiceProviderAddressRef;
import com.community.api.entity.Skill;
import com.community.api.services.*;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    private ResponseService responseService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    @Autowired
    private SanitizerService sanitizerService;

    @Transactional
    @PostMapping("/assign-skill")
    public ResponseEntity<?>addSkill(@RequestParam Long serviceProviderId,@RequestParam int skillId)
    {
        try {
            Skill skill = entityManager.find(Skill.class, skillId);
            ServiceProviderEntity serviceProviderEntity = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            List<Skill> listOfSkills = serviceProviderEntity.getSkills();
            listOfSkills.add(skill);
            serviceProviderEntity.setSkills(listOfSkills);
            entityManager.merge(serviceProviderEntity);
            return responseService.generateSuccessResponse("Skill assigned to service provider id : "+serviceProviderEntity.getService_provider_id(),serviceProviderEntity, HttpStatus.OK);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error assigning skill: " + e.getMessage());
        }
    }
    @PatchMapping("save-service-provider")
    public ResponseEntity<?> updateServiceProvider(@RequestParam Long userId, @RequestBody Map<String,Object> serviceProviderDetails) throws Exception {
        try{
        return serviceProviderService.updateServiceProvider(userId,serviceProviderDetails);
    }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error updating: " + e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @DeleteMapping("delete")
    public ResponseEntity<?>deleteServiceProvider(@RequestParam Long userId)
    {
        try{
        ServiceProviderEntity serviceProviderToBeDeleted=entityManager.find(ServiceProviderEntity.class,userId);
        if(serviceProviderToBeDeleted==null)
            return responseService.generateErrorResponse("No record found",HttpStatus.NOT_FOUND);
        else
            entityManager.remove(serviceProviderToBeDeleted);
        return responseService.generateSuccessResponse("Service Provider Deleted",null,HttpStatus.OK);
    }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting: " + e.getMessage());
        }}
    @Transactional
    @PostMapping("create-or-update-password")
    public ResponseEntity<?>deleteServiceProvider(@RequestBody Map<String,Object>passwordDetails,@RequestParam long userId)
    {
        try {
            if(!sharedUtilityService.validateInputMap(passwordDetails).equals(SharedUtilityService.ValidationResult.SUCCESS))
            {
                return ResponseService.generateErrorResponse("Invalid Request Body",HttpStatus.UNPROCESSABLE_ENTITY);
            }
            String password = (String) passwordDetails.get("password");
            passwordDetails=sanitizerService.sanitizeInputMap(passwordDetails);
           // String newPassword = (String) passwordDetails.get("newPassword");
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, userId);
            if (serviceProvider == null)
                return responseService.generateErrorResponse("No records found", HttpStatus.NOT_FOUND);
            if (serviceProvider.getPassword() == null) {
                serviceProvider.setPassword(passwordEncoder.encode(password));
                entityManager.merge(serviceProvider);
                return responseService.generateSuccessResponse("Password created",serviceProvider,HttpStatus.OK);
            } else {
                if (password == null /*|| newPassword == null*/)
                    return responseService.generateErrorResponse("Empty password entered", HttpStatus.BAD_REQUEST);
                /*if (passwordEncoder.matches(password, serviceProvider.getPassword())) {
                    serviceProvider.setPassword(passwordEncoder.encode(newPassword));*/
                if(!passwordEncoder.matches(password,serviceProvider.getPassword())) {
                    serviceProvider.setPassword(passwordEncoder.encode(password));
                    entityManager.merge(serviceProvider);
                    return responseService.generateSuccessResponse("New Password Set",serviceProvider, HttpStatus.OK);
                }
                    return responseService.generateErrorResponse("Old Password and new Password cannot be same",HttpStatus.BAD_REQUEST);
                 }/*else
                    return new ResponseEntity<>("Password do not match", HttpStatus.BAD_REQUEST);*/
            }
        catch (Exception e) {
                exceptionHandling.handleException(e);
                return responseService.generateErrorResponse("Error changing/updating password: " + e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
            }
    }
    @GetMapping("get-service-provider")
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
    @PostMapping("/add-address")
    public ResponseEntity<?> addAddress(@RequestParam long serviceProviderId,@RequestBody ServiceProviderAddress serviceProviderAddress) throws Exception {
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error adding address " + e.getMessage());
        }
    }

    @GetMapping("/get-address-names")
    public ResponseEntity<?> getAddressTypes()
    {
        try{
        TypedQuery<ServiceProviderAddressRef> query = entityManager.createQuery(Constant.jpql, ServiceProviderAddressRef.class);
        return responseService.generateSuccessResponse("List of addresses : ",query.getResultList(),HttpStatus.OK);
    }catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some issue in fetching addressNames " + e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }


    @Transactional
    @GetMapping("/get-all-service-providers")
    public ResponseEntity<?> getAllServiceProviders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit)
    {
        try {
            // Calculate the start position for pagination
            int startPosition = page * limit;
            // Create the query
            TypedQuery<ServiceProviderEntity> query = entityManager.createQuery(Constant.GET_ALL_SERVICE_PROVIDERS, ServiceProviderEntity.class);
            // Apply pagination
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);
            List<ServiceProviderEntity> results = query.getResultList();
            return ResponseService.generateSuccessResponse("List of service providers: ", results, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service providers: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @GetMapping("/get-all-details/{serviceProviderId}")
    public ResponseEntity<?> getAllDetails(@PathVariable Long serviceProviderId)
    {
        try
        {
            ServiceProviderEntity serviceProviderEntity= entityManager.find(ServiceProviderEntity.class,serviceProviderId);
            if(serviceProviderEntity==null)
            {
                return ResponseService.generateErrorResponse("Service provider does not found",HttpStatus.NOT_FOUND);
            }

            return ResponseService.generateSuccessResponse("Service Provider details retrieved successfully",serviceProviderEntity,HttpStatus.OK);
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service provider details " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @PatchMapping("/update-test-status/{serviceProviderId}")
    public ResponseEntity<?> updateTestStatus(@RequestBody UpdateTestStatusRank updateTestStatusRank, @PathVariable Long serviceProviderId) {
        try {
            return serviceProviderService.updateTestStatusRank(updateTestStatusRank,serviceProviderId);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error updating: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @GetMapping("/get-all-service-providers-with-completed-test")
    public ResponseEntity<?> getAllServiceProvidersWithCompletedTest(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit)
    {
        try {
            int startPosition = page * limit;

            TypedQuery<ServiceProviderEntity> query = entityManager.createQuery(
                    "SELECT s FROM ServiceProviderEntity s WHERE s.testStatus.test_status_id = :testStatusId",
                    ServiceProviderEntity.class);

            query.setParameter("testStatusId", 2L);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);

            List<ServiceProviderEntity> results = query.getResultList();
            if(results.isEmpty())
            {
                return ResponseService.generateSuccessResponse("There is no any service Provider who has completed the test", results, HttpStatus.OK);
            }

            return ResponseService.generateSuccessResponse("List of service providers with test_status 2: ", results, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service providers: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


}