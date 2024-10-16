package com.community.api.endpoint.avisoft.controller.order;
import com.community.api.component.Constant;
import com.community.api.dto.CustomProductWrapper;
import com.community.api.dto.PhysicalRequirementDto;
import com.community.api.dto.ReserveCategoryDto;
import com.community.api.entity.CombinedOrderDTO;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.OrderDTO;
import com.community.api.services.PhysicalRequirementDtoService;
import com.community.api.services.ReserveCategoryDtoService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/orders",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
)
public class OrderController
{
    private EntityManager entityManager;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ReserveCategoryDtoService reserveCategoryDtoService;
    @Autowired
    private PhysicalRequirementDtoService physicalRequirementDtoService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    public void setEntityManager(EntityManager entityManager)
    {
        this.entityManager=entityManager;
    }
    @RequestMapping(value = "get-order-history/{customerId}",method = RequestMethod.GET)
    public ResponseEntity<?> getOrderHistory(@PathVariable Long customerId, @RequestParam(defaultValue = "oldest-to-latest") String sort,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "5") int limit) {
        try {
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customerId);
            Map<String, Object> orderMap = new HashMap<>();
            if (customCustomer == null)
                return ResponseService.generateErrorResponse("Customer with the provided Id not found", HttpStatus.NOT_FOUND);
            if (customCustomer.getNumberOfOrders() == 0)
                return ResponseService.generateErrorResponse("Order History Empty - No Orders placed", HttpStatus.OK);
            String orderNumber = "O-" + customerId + "%"; // Use % for wildcard search
            int startPosition = page * limit;
            Query query = entityManager.createNativeQuery(Constant.GET_ORDERS_USING_CUSTOMER_ID);
            query.setFirstResult(startPosition);
            query.setMaxResults(limit);
            query.setParameter("orderNumber", orderNumber);
            List<BigInteger> orders = query.getResultList();
            List<CombinedOrderDTO> orderDetails = new ArrayList<>();
            OrderDTO orderDTO = null;
            for (BigInteger orderId : orders) {
                Order order = orderService.findOrderById(orderId.longValue());
                if (order != null) {
                    orderDTO = new OrderDTO(order.getId(),
                            order.getName(),
                            order.getTotal(),
                            order.getStatus(),
                            order.getSubmitDate(),
                            order.getOrderNumber(),
                            order.getEmailAddress(),
                            order.getCustomer().getId(),
                            order.getSubTotal());
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
                orderDetails.add(combinedOrderDTO);
            }
            if (sort.equals("latest-to-oldest"))
                sortOrdersByDate(orderDetails);
            else if (!sort.equals("oldest-to-latest"))
                return ResponseService.generateErrorResponse("Invalid sort option", HttpStatus.BAD_REQUEST);
            orderMap.put("Order List", orderDetails);
            return ResponseService.generateSuccessResponse("Orders", orderMap, HttpStatus.OK);

        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving cart Items", HttpStatus.INTERNAL_SERVER_ERROR);
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
            if (order != null) {
                orderDTO = new OrderDTO(order.getId(),
                        order.getName(),
                        order.getTotal(),
                        order.getStatus(),
                        order.getSubmitDate(),
                        order.getOrderNumber(),
                        order.getEmailAddress(),
                        order.getCustomer().getId(),
                        order.getSubTotal());
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
            return ResponseService.generateErrorResponse("Error retrieving cart Items", HttpStatus.INTERNAL_SERVER_ERROR);
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

}