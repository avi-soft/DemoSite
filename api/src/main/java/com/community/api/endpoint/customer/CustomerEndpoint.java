package com.community.api.endpoint.customer;
import com.community.api.component.Constant;
import com.community.api.services.CustomCustomerService;
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
import java.net.URLEncoder;

import static com.community.api.component.Constant.COUNTRY_CODE;

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
    @Autowired
    private TwilioService twilioService;
    @Autowired
    private CustomCustomerService customCustomerService;

    @RequestMapping(value = "getCustomer/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<Object> retrieveCustomerById(@PathVariable Long customerId) {
        try {
            if (customerService == null) {
                logger.error("Customer service is not initialized.");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Customer customer = customerService.readCustomerById(customerId);



            if (customer == null) {
                return new ResponseEntity<>("Customer with this ID does not exist", HttpStatus.NOT_FOUND);
            } else {
                CustomerDTO customerDTO=new CustomerDTO();
                customerDTO.setFirstName(customer.getFirstName());
                customerDTO.setLastName(customer.getLastName());
                customerDTO.setEmail(customer.getEmailAddress());
                customerDTO.setUsername(customer.getUsername());
                customerDTO.setCustomerId(customerDTO.getCustomerId());
                CustomCustomer customCustomer =em.find(CustomCustomer.class,customer.getId());
                if(customCustomer!=null) {
                    customerDTO.setMobileNumber(customCustomer.getMobileNumber());
                    return new ResponseEntity<>(customerDTO,HttpStatus.OK);
                }
                else
                {
                    return new ResponseEntity<>("Error fetching Customer Data",HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } catch (Exception e) {

            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error retrieving Customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Transactional
    @RequestMapping(value = "register", method = RequestMethod.POST)
    public ResponseEntity<String> addCustomer(@RequestBody CustomCustomer customerDetails) {
        try {
            if (customerService == null) {
                logger.error("Customer service is not initialized.");
                return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (!customCustomerService.validateInput(customerDetails))
                return new ResponseEntity<>("One or more inputs invalid", HttpStatus.UNPROCESSABLE_ENTITY);

            String countryCode = null;
            if (customerDetails.getCountryCode() == null || customerDetails.getCountryCode().isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }else{
                String encodedCountryCode = URLEncoder.encode(customerDetails.getCountryCode(), "UTF-8");

                countryCode = encodedCountryCode;
            }
            String updated_mobile = null;
            if (customerDetails.getMobileNumber().startsWith("0")) {
                 updated_mobile = customerDetails.getMobileNumber().substring(1);
            }else{
                updated_mobile = customerDetails.getMobileNumber();
            }
            CustomCustomer customerRecords = customCustomerService.findCustomCustomerByPhone(customerDetails.getMobileNumber(),countryCode);
            if (customerRecords != null) {

                return new ResponseEntity<>("Data already exists", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Customer customer = customerService.createCustomer();
            customerDetails.setId(customerService.findNextCustomerId());
            customerDetails.setMobileNumber(updated_mobile);
            customerDetails.setCountryCode(countryCode);
            em.persist(customerDetails);
            return new ResponseEntity<>("Customer Created succesfully " + customerDetails.getId(), HttpStatus.OK);
        }  catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error saving", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @RequestMapping(value = "update/{customerId}", method = RequestMethod.PUT)
     public ResponseEntity<String> updateCustomer(@RequestBody CustomCustomer customerDetails,@PathVariable Long customerId) {
         try {
             if (customerService == null) {
                 return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
             }
             /*if(!customCustomerService.validateInput(customerDetails))
                return new ResponseEntity<>("One or more inputs invalid", HttpStatus.UNPROCESSABLE_ENTITY);*/
             CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
             if (customerDetails.getMobileNumber() != null)
             {
                 if(customCustomerService.isValidMobileNumber(customerDetails.getMobileNumber())==false)
                     return new ResponseEntity<>("Error updating ,mobile number invalid", HttpStatus.INTERNAL_SERVER_ERROR);
             }
            customerDetails.setId(customerId);
            em.merge(customerDetails);
            return new ResponseEntity<>("Customer Updated", HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error updating", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @RequestMapping(value = "delete/{customerId}", method = RequestMethod.DELETE)
    public ResponseEntity<String> updateCustomer(@PathVariable Long customerId) {
        try {
            if (customerService == null) {
                return new ResponseEntity<>("Customer service is not initialized.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer=customerService.readCustomerById(customerId);
            if(customer!=null)
            {
                customerService.deleteCustomer(customerService.readCustomerById(customerId));
                return new ResponseEntity<>("Record Deleted Successfully", HttpStatus.OK);
            }
            else
            {
                return new ResponseEntity<>("No Records found for this ID", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error deleting", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "getCustomerbyphonenumber", method = RequestMethod.GET)
    public ResponseEntity<Object> retrieveCustomerByPhonee(@RequestParam("mobileNumber") String mobileNumber, @RequestParam(value = "countryCode", required = false) String countryCode,

                                                           HttpSession session) {
        try {
            if (customerService == null) {
                logger.error("Customer service is not initialized.");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (countryCode == null || countryCode.isEmpty()) {
                countryCode = Constant.COUNTRY_CODE;
            }else{
                String encodedCountryCode = URLEncoder.encode(countryCode, "UTF-8");

                countryCode = encodedCountryCode;
            }

            Customer customer = customCustomerService.findCustomCustomerByPhone(mobileNumber,countryCode);

            if (customer == null) {
                return new ResponseEntity<>("Customer with this ID does not exist", HttpStatus.NOT_FOUND);
            } else {
                CustomerDTO customerDTO=new CustomerDTO();
                customerDTO.setFirstName(customer.getFirstName());
                customerDTO.setLastName(customer.getLastName());
                customerDTO.setEmail(customer.getEmailAddress());
                customerDTO.setCustomerId(customerDTO.getCustomerId());
                customerDTO.setUsername(customer.getUsername());
                CustomCustomer customCustomer =em.find(CustomCustomer.class,customer.getId());
                if(customCustomer!=null) {
                    customerDTO.setMobileNumber(customCustomer.getMobileNumber());

                    return new ResponseEntity<>(customerDTO,HttpStatus.OK);
                }
                else
                {
                    return new ResponseEntity<>("Error fetching Customer Data",HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } catch (Exception e) {

            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error retrieving Customer", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
