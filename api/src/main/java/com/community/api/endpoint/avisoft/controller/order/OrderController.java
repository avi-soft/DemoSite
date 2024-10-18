package com.community.api.endpoint.avisoft.controller.order;
import com.community.api.component.Constant;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.PhysicalRequirementDto;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CombinedOrderDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.OrderDTO;
import com.community.api.entity.OrderRequest;
import com.community.api.services.CustomOrderService;
import com.community.api.services.PhysicalRequirementDtoService;
import com.community.api.services.ReserveCategoryDtoService;
import com.community.api.services.ResponseService;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping(value = "/orders",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
)
@RestController
public class OrderController
{
    private EntityManager entityManager;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ServiceProviderServiceImpl serviceProviderService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private CustomOrderService customOrderService;
    @Autowired
    private ReserveCategoryDtoService reserveCategoryDtoService;
    @Autowired
    private PhysicalRequirementDtoService physicalRequirementDtoService;
    @Autowired
    public void setEntityManager(EntityManager entityManager)
    {
        this.entityManager=entityManager;
    }
    @RequestMapping(value = "get-order-history/{customerId}",method = RequestMethod.GET)
    public ResponseEntity<?> getOrderHistory(@PathVariable Long customerId, @RequestParam(defaultValue = "oldest-to-latest") String sort,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "5") int limit)
    {
        try{
        CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customerId);
        if (customCustomer==null)
            return ResponseService.generateErrorResponse("Customer with the provided Id not found", HttpStatus.NOT_FOUND);
        if(customCustomer.getNumberOfOrders()==0)
            return ResponseService.generateErrorResponse("Order History Empty - No Orders placed", HttpStatus.OK);
        String orderNumber = "O-"+customerId+"%"; // Use % for wildcard search
        int startPosition=page*limit;
        Query query = entityManager.createNativeQuery(Constant.GET_ORDERS_USING_CUSTOMER_ID);
        query.setFirstResult(startPosition);
        query.setMaxResults(limit);
        query.setParameter("orderNumber", orderNumber);
        List<BigInteger> orders = query.getResultList();
        return generateCombinedDTO(orders,sort);
    } catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error assigning Request to Service Provider", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping(value = "details/{orderId}",method = RequestMethod.GET)
    public ResponseEntity<?> showClubbedOrders(@PathVariable Long orderId) {
        try {
            Long id = Long.valueOf(orderId);
            if (id == null)
                return ResponseService.generateErrorResponse("Order Id not specified", HttpStatus.BAD_REQUEST);
            Order order = orderService.findOrderById(orderId);
            OrderDTO orderDTO = null;
            if (order == null)
                return ResponseService.generateErrorResponse("Order not found", HttpStatus.BAD_REQUEST);
            CustomOrderState orderState=entityManager.find(CustomOrderState.class,order.getId());
            if (order != null) {
                orderDTO = new OrderDTO(order.getId(),
                        order.getName(),
                        order.getTotal(),
                        order.getStatus(),
                        order.getSubmitDate(),
                        order.getOrderNumber(),
                        order.getEmailAddress(),
                        order.getCustomer().getId(),
                        order.getSubTotal(),
                        orderState.getOrderState())
                      ;
            }
            OrderItem orderItem = order.getOrderItems().get(0);
            Long productId = Long.parseLong(orderItem.getOrderItemAttributes().get("productId").getValue());
            CustomProduct customProduct = entityManager.find(CustomProduct.class, productId);
            CustomProductWrapper customProductWrapper = null;
            if (customProduct != null) {
                customProductWrapper = new CustomProductWrapper();
                List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(productId);
                List<PhysicalRequirementDto> physicalRequirementDtoList = physicalRequirementDtoService.getPhysicalRequirementDto(productId);
                customProductWrapper.wrapDetails(customProduct, reserveCategoryDtoList, physicalRequirementDtoList);
            }
            CombinedOrderDTO combinedOrderDTO = new CombinedOrderDTO();
            combinedOrderDTO.setOrderDetails(orderDTO);
            combinedOrderDTO.setProductDetails(customProductWrapper);
            return ResponseService.generateSuccessResponse("Order Details :",combinedOrderDTO,HttpStatus.OK);
        } catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving Order Details", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
  @RequestMapping(value = "show-all-orders",method = RequestMethod.GET)
    public ResponseEntity<?> showClubbedOrders( @RequestParam(defaultValue = "all") String orderState,
                                                @RequestParam(defaultValue = "oldest-to-latest")String sort,
                                                @RequestParam(defaultValue = "0")int page,
                                               @RequestParam(defaultValue = "5")int limit) {
        try {
            String orderSearchQuery = "SELECT o.order_id FROM order_state o WHERE o.order_state =:orderState";
            sort = sort.toLowerCase();
            int startPosition = page * limit;
            List<BigInteger> orderIds = null;
            Query query = null;
            if (orderState.equals("all")) {
                query = entityManager.createNativeQuery(Constant.GET_ALL_ORDERS);
                query.setFirstResult(startPosition);
                query.setMaxResults(limit);
                orderIds = query.getResultList();
            } else {
                query = entityManager.createNativeQuery(Constant.SEARCH_ORDER_QUERY);
                query.setFirstResult(startPosition);
                query.setMaxResults(limit);
                switch (orderState) {
                    case "completed":
                        query.setParameter("orderStatus", Constant.ORDER_STATE_COMPLETED);
                        break;
                    case "in-review":
                        query.setParameter("orderStatus", Constant.ORDER_STATE_IN_REVIEW);
                        break;
                    case "in-progress":
                        query.setParameter("orderStatus", Constant.ORDER_STATE_IN_PROGRESS);
                        break;
                    case "auto-assigned":
                        query.setParameter("orderStatus", Constant.ORDER_STATE_AUTO_ASSIGNED);
                        break;
                    case "unassigned":
                        query.setParameter("orderStatus", Constant.ORDER_STATE_UNASSIGNED);
                        break;
                    case "new":
                        query.setParameter("orderStatus", Constant.ORDER_STATE_NEW);
                        break;
                    default:
                        return ResponseService.generateErrorResponse("Wrong search filter", HttpStatus.BAD_REQUEST);
                }
            }
            orderIds = query.getResultList();
            return generateCombinedDTO(orderIds, sort);
        } catch (Exception e)
    {
        exceptionHandling.handleException(e);
        return ResponseService.generateErrorResponse("Error assigning Request to Service Provider", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
    public void sortOrdersByDate(List<CombinedOrderDTO> orders) {
        Collections.sort(orders, new Comparator<CombinedOrderDTO>() {
            @Override
            public int compare(CombinedOrderDTO o1, CombinedOrderDTO o2) {
                return o2.getOrderDetails().getSubmitDate().compareTo(o1.getOrderDetails().getSubmitDate());
            }
        });
    }
    public ResponseEntity<?>generateCombinedDTO(List<BigInteger>orders,String sort)
    {
        try{
        System.out.println("4");
        Map<String,Object>orderMap=new HashMap<>();
        List<CombinedOrderDTO>orderDetails=new ArrayList<>();
        OrderDTO orderDTO=null;
        for(BigInteger orderId:orders)
        {
            Order order=orderService.findOrderById(orderId.longValue());
            CustomOrderState orderState=entityManager.find(CustomOrderState.class,order.getId());
            if(order!=null)
            {
               orderDTO = new OrderDTO(
                        order.getId(),
                        order.getName(),
                        order.getTotal(),
                        order.getStatus(),
                        order.getSubmitDate(),
                        order.getOrderNumber(),
                        order.getEmailAddress(),
                        order.getCustomer().getId(),
                        order.getSubTotal(),
                        orderState.getOrderState()
                );

            }
            OrderItem orderItem=order.getOrderItems().get(0);
            Long productId=Long.parseLong(orderItem.getOrderItemAttributes().get("productId").getValue());
            CustomProduct customProduct=entityManager.find(CustomProduct.class,productId);
            CustomProductWrapper customProductWrapper=null;
            if(customProduct!=null) {
                customProductWrapper = new CustomProductWrapper();
                List<ReserveCategoryDto> reserveCategoryDtoList = reserveCategoryDtoService.getReserveCategoryDto(productId);
                List<PhysicalRequirementDto> physicalRequirementDtoList = physicalRequirementDtoService.getPhysicalRequirementDto(productId);
                customProductWrapper.wrapDetails(customProduct, reserveCategoryDtoList, physicalRequirementDtoList);
            }
            CombinedOrderDTO combinedOrderDTO=new CombinedOrderDTO();
            combinedOrderDTO.setOrderDetails(orderDTO);
            combinedOrderDTO.setProductDetails(customProductWrapper);
            orderDetails.add(combinedOrderDTO);
        }
        if(sort.equals("latest-to-oldest"))
            sortOrdersByDate(orderDetails);
        else if(!sort.equals("oldest-to-latest"))
            return ResponseService.generateErrorResponse("Invalid sort option",HttpStatus.BAD_REQUEST);
        orderMap.put("Order List",orderDetails);
        return ResponseService.generateSuccessResponse("Orders",orderMap,HttpStatus.OK);
    }catch (Exception e)
    {
        exceptionHandling.handleException(e);
        return ResponseService.generateErrorResponse("Error assigning Request to Service Provider", HttpStatus.INTERNAL_SERVER_ERROR);
    }}
    @Transactional
    @RequestMapping(value = "assign-order/{orderId}/{serviceProviderId}",method = RequestMethod.POST)
    public ResponseEntity<?>manuallyAssignOrder(@PathVariable Long orderId,@PathVariable Long serviceProviderId) {
        try {
            Order order = orderService.findOrderById(orderId);
            if(order==null)
                return ResponseService.generateErrorResponse("Order not found",HttpStatus.NOT_FOUND);
            CustomOrderState customOrderState=entityManager.find(CustomOrderState.class,order.getId());
            if(!customOrderState.getOrderState().equals(Constant.ORDER_STATE_UNASSIGNED.getOrderState()))
                return ResponseService.generateErrorResponse("Cannot assign this order manually as its status is : "+customOrderState.getOrderState(),HttpStatus.UNPROCESSABLE_ENTITY);
            if (order == null)
                return ResponseService.generateErrorResponse("Order with the provided id not found", HttpStatus.NOT_FOUND);
            ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
            if (serviceProvider == null)
                return ResponseService.generateErrorResponse("Service Provider with the provided id not found", HttpStatus.NOT_FOUND);
            Query query = entityManager.createNativeQuery(Constant.GET_SP_ORDER_REQUEST);
            query.setParameter("orderId", orderId);
            query.setParameter("serviceProviderId", serviceProviderId);
            Integer queryResultCount=query.getResultList().size();
            if(queryResultCount==1)
            {
                return ResponseService.generateErrorResponse("Unable to assign order: This order request has been previously returned by the chosen service provider.",HttpStatus.BAD_REQUEST);
            }
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setOrderId(order.getId());
            orderRequest.setServiceProvider(serviceProvider);
            orderRequest.setRequestStatus("GENERATED");
            orderRequest.setGeneratedAt(LocalDateTime.now());
            orderRequest.setUpdatedAt(LocalDateTime.now());
            entityManager.persist(orderRequest);
            serviceProvider.getOrderRequests().add(orderRequest);
            order.setStatus(Constant.ORDER_STATUS_ASSIGNED);
            customOrderState.setOrderState(Constant.ORDER_STATE_ASSIGNED.getOrderState());
            customOrderState.setOrderStateDescription(Constant.ORDER_STATE_ASSIGNED.getOrderStateDescription());
            entityManager.merge(customOrderState);
            entityManager.merge(order);
            entityManager.merge(serviceProvider);
            return ResponseService.generateSuccessResponse("Order Request Generated", orderRequest, HttpStatus.OK);
        }catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error assigning Request to Service Provider", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @GetMapping("/{orderId}/availableSp")
    public ResponseEntity<?>getEligibleSp(@PathVariable Long orderId,@RequestParam (defaultValue = "0") int page ,@RequestParam(defaultValue = "10") int limit)
    {
        try{
            List<ServiceProviderEntity>result=customOrderService.availableSp(orderId,page,limit);
            return ResponseService.generateSuccessResponse("List of available Sp",result,HttpStatus.OK);
        }catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error viewing SP List", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
