package com.community.api.endpoint.avisoft.controller.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.PhysicalRequirementDto;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.*;
import com.community.api.services.DistrictService;
import com.community.api.services.ResponseService;
import com.community.api.services.*;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.OrderService;
import com.community.api.utils.ServiceProviderDocument;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/service-providers")
public class ServiceProviderController {

    @Autowired
    private ServiceProviderServiceImpl serviceProviderService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private OrderStatusByStateService orderStatusByStateService;
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
    @Autowired
    private OrderService orderService;
    @Autowired
    private ReserveCategoryDtoService reserveCategoryDtoService;
    @Autowired
    private PhysicalRequirementDtoService physicalRequirementDtoService;
    @Autowired
    private DummyAssignerService dummyAssignerService;

    @Transactional
    @PostMapping("/assign-skill")
    public ResponseEntity<?> addSkill(@RequestParam Long serviceProviderId, @RequestParam int skillId) {
        try {
            Skill skill = entityManager.find(Skill.class, skillId);
            ServiceProviderEntity serviceProviderEntity = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            List<Skill> listOfSkills = serviceProviderEntity.getSkills();
            listOfSkills.add(skill);
            serviceProviderEntity.setSkills(listOfSkills);
            entityManager.merge(serviceProviderEntity);
            return responseService.generateSuccessResponse("Skill assigned to service provider id : " + serviceProviderEntity.getService_provider_id(), serviceProviderEntity, HttpStatus.OK);
        }  catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error assigning skill: " + e.getMessage());
        }
    }

    @PatchMapping("save-service-provider")
    public ResponseEntity<?> updateServiceProvider(@RequestParam Long userId, @RequestBody Map<String, Object> serviceProviderDetails) throws Exception {
        try {
            ServiceProviderEntity serviceProvider=entityManager.find(ServiceProviderEntity.class,userId);
            if(serviceProvider==null)
                return ResponseService.generateErrorResponse("Service Provider with provided Id not found",HttpStatus.NOT_FOUND);
            return serviceProviderService.updateServiceProvider(userId, serviceProviderDetails);
        }  catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error updating: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @DeleteMapping("delete")
    public ResponseEntity<?> deleteServiceProvider(@RequestParam Long userId) {
        try {
            ServiceProviderEntity serviceProviderToBeDeleted = entityManager.find(ServiceProviderEntity.class, userId);
            if (serviceProviderToBeDeleted == null)
                return responseService.generateErrorResponse("No record found", HttpStatus.NOT_FOUND);
            else
                entityManager.remove(serviceProviderToBeDeleted);
            return responseService.generateSuccessResponse("Service Provider Deleted", null, HttpStatus.OK);
        }  catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error deleting: " + e.getMessage());
        }
    }

    @Transactional
    @PostMapping("create-or-update-password")
    public ResponseEntity<?> deleteServiceProvider(@RequestBody Map<String, Object> passwordDetails, @RequestParam long userId) {
        try {
            if (!sharedUtilityService.validateInputMap(passwordDetails).equals(SharedUtilityService.ValidationResult.SUCCESS)) {
                return ResponseService.generateErrorResponse("Invalid Request Body", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            String password = (String) passwordDetails.get("password");
            passwordDetails = sanitizerService.sanitizeInputMap(passwordDetails);
            // String newPassword = (String) passwordDetails.get("newPassword");
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, userId);
            if (serviceProvider == null)
                return responseService.generateErrorResponse("No records found", HttpStatus.NOT_FOUND);
            if (serviceProvider.getPassword() == null) {
                serviceProvider.setPassword(passwordEncoder.encode(password));
                entityManager.merge(serviceProvider);
                return responseService.generateSuccessResponse("Password created", serviceProvider, HttpStatus.OK);
            } else {
                if (password == null /*|| newPassword == null*/)
                    return responseService.generateErrorResponse("Empty password entered", HttpStatus.BAD_REQUEST);
                /*if (passwordEncoder.matches(password, serviceProvider.getPassword())) {
                    serviceProvider.setPassword(passwordEncoder.encode(newPassword));*/
                if (!passwordEncoder.matches(password, serviceProvider.getPassword())) {
                    serviceProvider.setPassword(passwordEncoder.encode(password));
                    entityManager.merge(serviceProvider);
                    return responseService.generateSuccessResponse("New Password Set", serviceProvider, HttpStatus.OK);
                }
                return responseService.generateErrorResponse("Old Password and new Password cannot be same", HttpStatus.BAD_REQUEST);
            }/*else
                    return new ResponseEntity<>("Password do not match", HttpStatus.BAD_REQUEST);*/
        }  catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error changing/updating password: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
        }  catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Some fetching account " + e.getMessage());
        }
    }

    @Transactional
    @PostMapping("/add-address")
    public ResponseEntity<?> addAddress(@RequestParam long serviceProviderId, @RequestBody ServiceProviderAddress serviceProviderAddress) throws Exception {
        try {
            if (serviceProviderAddress == null) {
                return responseService.generateErrorResponse("Incomplete Details", HttpStatus.BAD_REQUEST);
            }
            ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (existingServiceProvider == null) {
                return responseService.generateErrorResponse("Service Provider Not found", HttpStatus.BAD_REQUEST);
            }
            List<ServiceProviderAddress> addresses = existingServiceProvider.getSpAddresses();
            serviceProviderAddress.setState(districtService.findStateById(Integer.parseInt(serviceProviderAddress.getState())));
            serviceProviderAddress.setDistrict(districtService.findDistrictById(Integer.parseInt(serviceProviderAddress.getDistrict())));
            addresses.add(serviceProviderAddress);
            existingServiceProvider.setSpAddresses(addresses);
            serviceProviderAddress.setServiceProviderEntity(existingServiceProvider);
            entityManager.persist(serviceProviderAddress);
            entityManager.merge(existingServiceProvider);
            return responseService.generateSuccessResponse("Address added successfully", serviceProviderAddress, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error adding address " + e.getMessage());
        }
    }

    @GetMapping("/get-address-names")
    public ResponseEntity<?> getAddressTypes() {
        try {
            TypedQuery<ServiceProviderAddressRef> query = entityManager.createQuery(Constant.jpql, ServiceProviderAddressRef.class);
            return responseService.generateSuccessResponse("List of addresses : ", query.getResultList(), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some issue in fetching addressNames " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @Transactional
    @GetMapping("/get-all-service-providers")
    public ResponseEntity<?> getAllServiceProviders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        try{
            int startPosition = page * limit;
            // Create the query
            TypedQuery<ServiceProviderEntity> query = entityManager.createQuery(Constant.GET_ALL_SERVICE_PROVIDERS, ServiceProviderEntity.class);
            // Apply pagination
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);
            List<ServiceProviderEntity> results = query.getResultList();
            List<Map<String,Object>>resultOfSp=new ArrayList<>();
            for(ServiceProviderEntity serviceProvider: results)
            {
                resultOfSp.add(sharedUtilityService.serviceProviderDetailsMap(serviceProvider));
            }


            return ResponseService.generateSuccessResponse("List of service providers: ", resultOfSp, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service providers: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    @GetMapping("/get-all-details/{serviceProviderId}")
    public ResponseEntity<?> getAllDetails(@PathVariable Long serviceProviderId) {
        try {
            ServiceProviderEntity serviceProviderEntity = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (serviceProviderEntity == null) {
                return ResponseService.generateErrorResponse("Service provider does not found", HttpStatus.NOT_FOUND);
            }

            Map<String,Object> serviceProviderMap= sharedUtilityService.serviceProviderDetailsMap(serviceProviderEntity);
            return ResponseService.generateSuccessResponse("Service Provider details retrieved successfully", serviceProviderMap, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service provider details " + e.getMessage(), HttpStatus.BAD_REQUEST);
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

            query.setParameter("testStatusId", Constant.TEST_COMPLETED_STATUS);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);

            List<ServiceProviderEntity> results = query.getResultList();
            if(results.isEmpty())
            {
                return ResponseService.generateSuccessResponse("There is no any service Provider who has completed the test", results, HttpStatus.OK);
            }

            return ResponseService.generateSuccessResponse("List of service providers with completed test status: ", results, HttpStatus.OK);
        }  catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service providers: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/filter-service-provider")
    public ResponseEntity<?> filterServiceProvider(@RequestParam(required = false) String state,
                                                   @RequestParam(required = false) String district,
                                                   @RequestParam(required = false) String first_name,
                                                   @RequestParam(required = false) String last_name,
                                                   @RequestParam(required = false) String mobileNumber) {
        try {
            if(first_name==null&&last_name==null&&state==null&&district==null&&mobileNumber==null)
            {
                return ResponseService.generateErrorResponse("Need to provide atleast one search filter",HttpStatus.BAD_REQUEST);
            }
            return ResponseService.generateSuccessResponse("Service Providers", serviceProviderService.searchServiceProviderBasedOnGivenFields(state, district, first_name, last_name, mobileNumber), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching service provider details " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @Transactional
    @GetMapping("/show-referred-candidates/{service_provider_id}")
    public ResponseEntity<?> showRefferedCandidates (@PathVariable Long service_provider_id){
        try {
            ServiceProviderEntity serviceProvider=entityManager.find(ServiceProviderEntity.class,service_provider_id);
            if(serviceProvider==null)
                return ResponseService.generateErrorResponse("Service Provider not found",HttpStatus.NOT_FOUND);
            List<Map<String, Object>> customers = new ArrayList<>();
            for(CustomerReferrer customerReferrer:serviceProvider.getMyReferrals())
            {
                customers.add(sharedUtilityService.breakReferenceForCustomer(customerReferrer.getCustomer()));
            }
            return ResponseService.generateSuccessResponse("List of referred candidates is : ", customers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching candidates: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @Transactional
    @GetMapping("/{serviceProviderId}/order-requests")
    public ResponseEntity<?> allOrderRequestsBySPId(@PathVariable Long serviceProviderId,@RequestParam(defaultValue = "0")int page,@RequestParam(defaultValue = "10") int limit,@RequestParam(defaultValue = "all") String requestStatus)
    {
        try{
            int startPosition=page*limit;
            Query query=null;
            requestStatus=requestStatus.toLowerCase();
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (serviceProvider == null)
                return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.NOT_FOUND);
            if(requestStatus.equals("all"))
            {
                query =entityManager.createNativeQuery(Constant.GET_ONE_SP_ALL_ORDER_REQUEST);
            }
            else {
                query = entityManager.createNativeQuery(Constant.GET_ONE_SP_ORDER_REQUEST);
                switch (requestStatus) {
                    case "accepted":
                        query.setParameter("requestStatus", "ACCEPTED");
                        break;
                    case "returned":
                        query.setParameter("requestStatus", "RETURNED");
                        break;
                    case "new":
                        query.setParameter("requestStatus", "GENERATED");
                        break;
                    default:
                        return ResponseService.generateErrorResponse("Invalid Order request Status", HttpStatus.BAD_REQUEST);
                }
            }
            query.setParameter("serviceProviderId",serviceProviderId);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);
            List<BigInteger>orderRequestIds=query.getResultList();
            List<OrderRequest>spOrderRequests=new ArrayList<>();
            for (BigInteger orderRequestId:orderRequestIds)
            {
                OrderRequest orderRequest=entityManager.find(OrderRequest.class,orderRequestId.longValue());
                if(orderRequest!=null)
                    spOrderRequests.add(orderRequest);
            }
            return ResponseService.generateSuccessResponse("Order Requests :",spOrderRequests,HttpStatus.OK);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching candidates: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @Transactional
    @PostMapping("/{serviceProviderId}/order-requests/{orderRequestId}")
    public ResponseEntity<?> orderRequestAction(@PathVariable Long serviceProviderId,@PathVariable Long orderRequestId,@RequestParam String action,@RequestParam(required = false) Integer statusId) {
        try {
            action=action.toUpperCase();
            OrderRequest orderRequest = entityManager.find(OrderRequest.class, orderRequestId);
            if(orderRequest==null)
                return ResponseService.generateErrorResponse("Order Request Not found",HttpStatus.BAD_REQUEST);
            Order order = orderService.findOrderById(orderRequest.getOrderId());
            if (order == null)
                return ResponseService.generateErrorResponse("Order not found", HttpStatus.NOT_FOUND);
            if(!action.equals(Constant.SP_REQUEST_ACTION_VIEW)&&!action.equals(Constant.SP_REQUEST_ACTION_ACCEPT)&&!action.equals(Constant.SP_REQUEST_ACTION_RETURN))
                return ResponseService.generateErrorResponse("Invalid Action", HttpStatus.BAD_REQUEST);
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if(!orderRequest.getServiceProvider().equals(serviceProvider))
                return ResponseService.generateErrorResponse("Order Request does not belong to the specified SP,Check again",HttpStatus.BAD_REQUEST);
            if (serviceProvider == null)
                return ResponseService.generateErrorResponse("Service Provider not found", HttpStatus.NOT_FOUND);
            CustomOrderState orderState=entityManager.find(CustomOrderState.class,orderRequest.getOrderId());
            if (!orderRequest.getRequestStatus().equals("GENERATED")) {
                return ResponseService.generateErrorResponse("Order already Accepted/Returned ", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            if(action.equals(Constant.SP_REQUEST_ACTION_VIEW))
            {
                Long productId=Long.parseLong(order.getOrderItems().get(0).getOrderItemAttributes().get("productId").getValue());
                CustomProduct customProduct=entityManager.find(CustomProduct.class,productId);
                Map<String,Object>orderRequestDetail=new HashMap<>();
                OrderDTO orderDTO=new OrderDTO(
                        order.getId(),
                        order.getName(),
                        order.getTotal(),
                        order.getStatus(),
                        order.getSubmitDate(),
                        order.getOrderNumber(),
                        order.getEmailAddress(),
                        order.getCustomer().getId(),
                        order.getSubTotal(),
                        orderState.getOrderStateId() // Ensure this matches the expected order
                );

                CustomProductWrapper customProductWrapper = new CustomProductWrapper();
                List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(productId);
                List<PhysicalRequirementDto> physicalRequirementDtoList = physicalRequirementDtoService.getPhysicalRequirementDto(productId);
                customProductWrapper.wrapDetails(customProduct, reserveCategoryDtoList, physicalRequirementDtoList);
                orderRequestDetail.put("order_request_details",orderRequest);
                orderRequestDetail.put("order_details",orderDTO);
                orderRequestDetail.put("ordered_product_details",customProductWrapper);
                return ResponseService.generateSuccessResponse("Order Request Details :",orderRequestDetail,HttpStatus.OK);
            }
            else if (action.equals(Constant.SP_REQUEST_ACTION_ACCEPT)) {
                order.setStatus(Constant.ORDER_STATUS_IN_PROGRESS);
                ServiceProviderAcceptedOrders serviceProviderAcceptedOrders = new ServiceProviderAcceptedOrders();
                orderRequest.setRequestStatus("ACCEPTED");
                orderState.setOrderStateId(Constant.ORDER_STATE_IN_PROGRESS.getOrderStateId());
                Integer orderStatusId=orderStatusByStateService.getOrderStatusByOrderStateId(Constant.ORDER_STATE_IN_PROGRESS.getOrderStateId()).get(0).getOrderStatusId();
                orderState.setOrderStatusId(orderStatusId);
                orderRequest.setUpdatedAt(LocalDateTime.now());
                entityManager.merge(orderRequest);
                serviceProviderAcceptedOrders.setServiceProvider(serviceProvider);
                serviceProviderAcceptedOrders.setOrderId(orderRequest.getOrderId());
                serviceProviderAcceptedOrders.setGeneratedAt(LocalDateTime.now());
                serviceProviderAcceptedOrders.setUpdatedAt(LocalDateTime.now());
                entityManager.persist(serviceProviderAcceptedOrders);
                serviceProvider.getAcceptedOrders().add(serviceProviderAcceptedOrders);
                entityManager.merge(orderState);
                entityManager.merge(serviceProvider);
                return ResponseService.generateSuccessResponse("Order Accepted", null, HttpStatus.OK);
            } else  {
                orderRequest.setRequestStatus("RETURNED");
                order.setStatus(Constant.ORDER_STATUS_UNASSIGNED);
                if(statusId!=null)
                {
                    CustomOrderStatus customOrderStatus=entityManager.find(CustomOrderStatus.class,statusId);
                    if(customOrderStatus==null)
                    {
                        return ResponseService.generateErrorResponse("Invalid Order Status selected",HttpStatus.BAD_REQUEST);
                    }
                    if(!orderStatusByStateService.getOrderStatusByOrderStateId(Constant.ORDER_STATE_RETURNED.getOrderStateId()).contains(customOrderStatus))
                    {
                       return ResponseService.generateErrorResponse("Selected order Status does not belong to this action",HttpStatus.BAD_REQUEST);
                    }
                    orderState.setOrderStatusId(statusId);
                }
                else
                    return ResponseService.generateErrorResponse("Need to provide return status",HttpStatus.BAD_REQUEST);
                orderState.setOrderStatusId(statusId);
                orderState.setOrderStateId(Constant.ORDER_STATE_RETURNED.getOrderStateId());
                /*entityManager.merge(order);*/
                entityManager.merge(orderRequest);
                entityManager.merge(orderState);
                dummyAssignerService.dummyAssigner(order);
                return ResponseService.generateSuccessResponse("Order Returned", null, HttpStatus.OK);
            }
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Some issue in fetching order Requests: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @Transactional
    @RequestMapping(value = "/{serviceProviderId}/completeOrder/{orderRequestId}",method = RequestMethod.PUT)
    public ResponseEntity<?>completeOrder(@PathVariable Long serviceProviderId,@PathVariable Long orderRequestId,@RequestParam Integer statusId) {
        try {
            OrderRequest orderRequest=entityManager.find(OrderRequest.class,orderRequestId);
            if(orderRequest==null)
                return ResponseService.generateErrorResponse("Order Request not found",HttpStatus.NOT_FOUND);
            CustomOrderState customOrderState=entityManager.find(CustomOrderState.class,orderRequest.getOrderId());
            if(Constant.ORDER_STATE_COMPLETED.getOrderStateId().equals(customOrderState.getOrderStateId()))
            {
                return ResponseService.generateErrorResponse("Order Already Completed",HttpStatus.BAD_REQUEST);
            }
            ServiceProviderEntity serviceProvider=entityManager.find(ServiceProviderEntity.class,serviceProviderId);
            if(serviceProvider==null)
            {
                return ResponseService.generateErrorResponse("Service Provider not found",HttpStatus.NOT_FOUND);
            }
            if(!orderRequest.getServiceProvider().equals(serviceProvider))
                return ResponseService.generateErrorResponse("Order Request does not belong to the specified SP,Check again",HttpStatus.BAD_REQUEST);
            if(!customOrderState.getOrderStateId().equals(Constant.ORDER_STATE_IN_PROGRESS.getOrderStateId()))
                return ResponseService.generateErrorResponse("Cannot complete this order manually as its status is : "+orderStatusByStateService.getOrderStateById(customOrderState.getOrderStateId()).getOrderStateName(),HttpStatus.UNPROCESSABLE_ENTITY);
            if(statusId!=null)
            {
                CustomOrderStatus customOrderStatus=entityManager.find(CustomOrderStatus.class,statusId);
                if(customOrderStatus==null)
                {
                    return ResponseService.generateErrorResponse("Invalid Order Status selected",HttpStatus.BAD_REQUEST);
                }
                if(!orderStatusByStateService.getOrderStatusByOrderStateId(Constant.ORDER_STATE_COMPLETED.getOrderStateId()).contains(customOrderStatus))
                {
                    return ResponseService.generateErrorResponse("Selected order Status does not belong to this action",HttpStatus.BAD_REQUEST);
                }
                customOrderState.setOrderStatusId(Constant.ORDER_STATE_COMPLETED.getOrderStatusId());
                customOrderState.setOrderStatusId(statusId);
                entityManager.merge(customOrderState);
                Map<String,Object>response=new HashMap<>();
                response.put("order_id",orderRequest.getOrderId());
                response.put("order_request_id",orderRequestId);
                return ResponseService.generateSuccessResponse("Order Completed",response,HttpStatus.OK);
            }
            else
                return ResponseService.generateErrorResponse("Select an order completion status",HttpStatus.BAD_REQUEST);
        }catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error assigning Request to Service Provider", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}