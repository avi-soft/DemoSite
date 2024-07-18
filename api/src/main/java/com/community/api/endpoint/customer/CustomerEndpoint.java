package com.community.api.endpoint.customer;
import com.community.api.services.TwilioService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

@RestController
@RequestMapping(value = "/customer-custom",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)

public class CustomerEndpoint {
    String phoneQuery = "SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber";
    private static final Logger logger = LoggerFactory.getLogger(CustomerEndpoint.class);
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private EntityManager em;
    @Autowired
    private TwilioService twilioService;

    @RequestMapping(value = "getCustomer/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<Object> retrieveCustomerById(@PathVariable Long customerId) {
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

            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error retrieving Customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    @Transactional
    @RequestMapping(value = "register", method = RequestMethod.POST)
    public ResponseEntity<String> addCustomer(@RequestBody CustomCustomer customerDetails) {
        logger.debug("Adding Customer");
        try {
            if (customerService == null) {
                logger.error("Customer service is not initialized.");
                return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if(!validateInput(customerDetails))
                return new ResponseEntity<>("One or more inputs invalid", HttpStatus.UNPROCESSABLE_ENTITY);

            Customer customer = customerService.createCustomer();
            customerDetails.setId(customerService.findNextCustomerId());
            /*customer.setUsername(customerDetails.getUsername());
            customer.setPassword(customerDetails.getPassword());*/
            em.persist(customerDetails);
            return new ResponseEntity<>("Customer Saved", HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error saving", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Boolean validateInput(CustomCustomer customer) {
        if (customer.getUsername().isEmpty() || customer.getUsername() == null || customer.getMobileNumber().isEmpty() || customer.getMobileNumber() == null || customer.getPassword() == null || customer.getPassword().isEmpty())
            return false;
        if(!isValidMobileNumber(customer.getMobileNumber()))
            return false;
        return true;
    }
    private boolean isValidMobileNumber(String mobileNumber) {
        String mobileNumberPattern = "^\\+?\\d{10,13}$";
        return Pattern.compile(mobileNumberPattern).matcher(mobileNumber).matches();
    }

}
