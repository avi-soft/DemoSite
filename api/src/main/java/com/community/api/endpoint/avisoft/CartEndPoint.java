package com.community.api.endpoint.avisoft;
import com.broadleafcommerce.rest.api.endpoint.BaseEndpoint;
import com.broadleafcommerce.rest.api.endpoint.order.CartEndpoint;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.OrderService;

import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.broadleafcommerce.profile.web.core.CustomerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping(value = "/cart-custom",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class CartEndPoint extends BaseEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(CartEndpoint.class);
    @Autowired
    private CustomerService customerService;
    @Autowired
    OrderService orderService;

    @RequestMapping(value = "getCart/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<String> retrieveProductById(@PathVariable Long customerId) {
        logger.debug("Retrieving customer by ID: {}", customerId);
        Customer customer = customerService.readCustomerById(customerId);//finding the customer to get cart associated with it
        try {
            if (customerService == null) {
                logger.error("Customer service is not initialized.");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Order cart = null;
            if (customer == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                cart = this.orderService.findCartForCustomer(customer);
                if (cart == null) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                } else {
                    return new ResponseEntity<>(cart.getName(), HttpStatus.OK);
                }
            }
        } catch (RuntimeException e) {
            logger.error("Error retrieving Cart: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "createCart/{customerId}", method = RequestMethod.POST)
    public ResponseEntity<String> createCartForCustomer(@PathVariable Long customerId) {
        logger.debug("Retrieving customer by ID: {}", customerId);
        Customer customer = customerService.readCustomerById(customerId);//finding the customer to get cart associated with it
        try {
            if (customerService == null) {
                logger.error("Customer service is not initialized.");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Order cart = null;
            if (customer == null) {
                customer = this.customerService.createNewCustomer();
                customer.setAnonymous(true);
                CustomerState.setCustomer(customer);
            }
                cart = this.orderService.createNewCartForCustomer(customer);
                cart.setCustomer(customer);
                cart.setName("Customer X");
                return new ResponseEntity<>("Cart Created", HttpStatus.OK);
        }
        catch (RuntimeException e) {
            logger.error("Error retrieving Cart: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping(value = "deleteCart/{customerId}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteOrderFromCart(@PathVariable Long customerId) {
        logger.debug("Retrieving customer by ID: {}", customerId);
        Customer customer = customerService.readCustomerById(customerId);//finding the customer to get cart associated with it
        try {
            if (customerService == null) {
                logger.error("Customer service is not initialized.");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Order cart = null;
            if (customer == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                cart = this.orderService.findCartForCustomer(customer);

                if (cart == null) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                } else {
                    orderService.deleteOrder(cart);
                    return new ResponseEntity<>("Order Deleted", HttpStatus.OK);
                }
            }
        } catch (RuntimeException e) {
            logger.error("Error retrieving Cart: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}


