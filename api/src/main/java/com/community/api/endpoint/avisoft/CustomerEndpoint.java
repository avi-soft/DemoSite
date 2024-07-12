package com.community.api.endpoint.avisoft;

import com.broadleafcommerce.rest.api.endpoint.order.CartEndpoint;
import com.community.api.services.ExceptionHandlingImplement;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
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

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/customer-custom",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class CustomerEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(CustomerEndpoint.class);
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @RequestMapping(value = "getCustomer/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<Object> retrieveProductById(@PathVariable Long customerId) {
        logger.debug("Retrieving customer by ID: {}", customerId);
        try {
            if (customerService == null) {
                logger.error("Customer service is not initialized.");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return new ResponseEntity<>("Customer with this ID does not exist", HttpStatus.NOT_FOUND);
            } else {
                    return new ResponseEntity<>(customer, HttpStatus.OK);
            }
        } catch (Exception e) {
            logger.error("Error retrieving Cart: {}", e.getMessage(), e); //updated exception
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error retrieving Cart", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
