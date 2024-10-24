package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.CreateTicketDto;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomOrderState;
import com.community.api.entity.CustomProduct;
import com.community.api.entity.CustomServiceProviderTicket;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.entity.CustomTicketType;
import com.community.api.entity.CustomerReferrer;
import com.community.api.entity.OrderStateRef;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ServiceProviderTicketService {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderTicketService.class);

    @Autowired
    ServiceProviderServiceImpl serviceProviderService;

    @Autowired
    OrderStateRefService orderStateRefService;

    @Autowired
    CustomOrderService customOrderService;

    @Autowired
    OrderService orderService;

    @Autowired
    EntityManager entityManager;

    public void randomBindingTicketAllocation() {
        try{
            // we are fetching SP who are in approved state (later we will do only active)
            List<Map<String,Object>> availableServiceProvider = (List<Map<String, Object>>) serviceProviderService.searchServiceProviderBasedOnGivenFields(null,null,null,null,null, 1L);

            // later will do order which are in particular state. (write now just fetching which are in state 2).
            OrderStateRef orderStateRef = orderStateRefService.getOrderStateByOrderStateId(2);
            List<CustomOrderState> customOrders = customOrderService.getCustomOrdersByOrderStateId(orderStateRef.getOrderStateId());
            logger.info("Total Orders at the moments are: " + customOrders.size());

            List<Order> orders = new ArrayList<>();
            for(CustomOrderState customOrderState: customOrders) {

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(customOrderState);
                logger.info(jsonString);

                Order order = orderService.findOrderById(customOrderState.getOrderId());
                CustomCustomer customer = entityManager.find(CustomCustomer.class, order.getCustomer().getId());
                String customerString = objectMapper.writeValueAsString(customer);
                logger.info(customerString);

//                Query query = entityManager.createQuery(Constant.GET_ORDERS_BY_ORDER_STATE_ID, CustomOrderState.class);
//                query.setParameter("orderStateId", orderStateRef.getOrderStateId());
//                List<CustomOrderState> orderState = query.getResultList();
                List<CustomerReferrer> referrers = customer.getMyReferrer();
                System.out.println("Referrer list:" + referrers.size());
                for(CustomerReferrer refferer: referrers) {
                    ServiceProviderEntity serviceProvider = refferer.getServiceProvider();
                    System.out.println("REFFEREER ID: " + serviceProvider.getService_provider_id());

                    if(serviceProvider.getIsActive() ) {
                        // create a entry in serviceProvider tickets tables where the info about which serviceProvider is linked with which ticket is stored.
                        CreateTicketDto createTicketDto = new CreateTicketDto();
                        createTicketDto.setTicketState(1L);
                        createTicketDto.setTicketType(1L);
                        createTicketDto.setTicketStatus(1L);
                        createTicketDto.setAssignTo(serviceProvider.getService_provider_id());
                        CustomServiceProviderTicket serviceProviderTicket = createTicket(createTicketDto);
                    }

                }

                Long pid= Long.valueOf(order.getOrderItems().get(0).getOrderItemAttributes().get("productId").getValue());
                CustomProduct customProduct= entityManager.find(CustomProduct.class, pid);

            }

        } catch (Exception exception) {
            System.out.println("Exception caught: " + exception.getMessage());
        }
    }

    @Transactional
    public CustomServiceProviderTicket createTicket(CreateTicketDto createTicketDto) throws Exception {
        try{
            CustomServiceProviderTicket customServiceProviderTicket = new CustomServiceProviderTicket();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Set active start date to current date and time in "yyyy-MM-dd HH:mm:ss" format
            String formattedDate = dateFormat.format(new Date());
            Date createdDate = dateFormat.parse(formattedDate);

            if(createTicketDto.getTargetCompletionDate()!= null) {
                dateFormat.parse(dateFormat.format(createTicketDto.getTargetCompletionDate()));
                if(!createTicketDto.getTargetCompletionDate().after(createdDate)) {
                    ResponseService.generateErrorResponse("TARGET COMPLETION DATE MUST BE OF FUTURE", HttpStatus.NOT_FOUND);
                }
            }else{
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(createdDate);
                calendar.add(Calendar.HOUR_OF_DAY, 4);
                Date newTargetDate = calendar.getTime();

                createTicketDto.setTargetCompletionDate(newTargetDate);
            }

            customServiceProviderTicket.setTargetCompletionDate(createTicketDto.getTargetCompletionDate());
            customServiceProviderTicket.setCreatedDate(createdDate);

            customServiceProviderTicket = entityManager.merge(customServiceProviderTicket);
            return customServiceProviderTicket;
        } catch (Exception exception) {
            throw new Exception("Some Exception Caught: " + exception.getMessage());
        }
    }

    public void verticalDistributionTicketAllocation() {

    }

}
