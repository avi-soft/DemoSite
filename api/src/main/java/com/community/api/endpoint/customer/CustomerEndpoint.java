package com.community.api.endpoint.customer;
import com.community.api.services.ExceptionHandlingImplement;
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
import javax.transaction.Transactional;

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
    @Autowired
    private EntityManager em;
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
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public ResponseEntity<Object> LoginForCustomer(@RequestBody CustomCustomer customerDetails) {
        try {
            if (customerService == null) {
                logger.error("Customer service is not initialized.");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (customerDetails.getMobileNumber() == null || customerDetails.getCustomerPassword() == null) {
                return new ResponseEntity<>("Invalid mobile number or password", HttpStatus.BAD_REQUEST);
            }

            logger.debug("Mobile Number Provided: " + customerDetails.getMobileNumber());

            CustomCustomer existingCustomer = em.createQuery("SELECT c FROM CustomCustomer c WHERE c.mobileNumber = :mobileNumber", CustomCustomer.class)
                    .setParameter("mobileNumber", customerDetails.getMobileNumber())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (existingCustomer != null) {
                if (existingCustomer.getCustomerPassword().equals(customerDetails.getCustomerPassword())) {
                    return new ResponseEntity<>("Log in Successfull",HttpStatus.OK);
                } else {
                    return new ResponseEntity<>("Incorrect Password", HttpStatus.UNAUTHORIZED);
                }
            } else {
                return new ResponseEntity<>("Customer does not exist", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error logging in", e);
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
            Customer customer = customerService.createCustomer();
            long id = System.currentTimeMillis();
            customerDetails.setId(customerService.findNextCustomerId());
            customerService.saveCustomer(customer);
            em.merge(customerDetails);
            return new ResponseEntity<>("Customer Saved", HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error saving", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
