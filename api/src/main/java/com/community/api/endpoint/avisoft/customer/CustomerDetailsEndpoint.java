package com.community.api.endpoint.avisoft.customer;

import com.broadleafcommerce.rest.api.endpoint.BaseEndpoint;
import com.broadleafcommerce.rest.api.wrapper.CustomerWrapper;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.ws.rs.QueryParam;
import javax.ws.rs.GET;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
/*@Path("/customcustomer")
@Produces({ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
@Consumes({ MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })*/
@RequestMapping(value = "/customcustomer",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
public class CustomerDetailsEndpoint extends BaseEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerDetailsEndpoint.class);

    @Autowired
    protected CustomerService customerService;

    @GET
    public ResponseEntity<CustomerWrapper> getCustomer(@QueryParam("emailId") String emailId) {
        if (emailId == null || emailId.isEmpty()) {
            LOGGER.error("Email ID is required");
            return ResponseEntity.badRequest().build();
        }
        try {
            CustomerWrapper customerWrapper = new CustomerWrapper();
            customerWrapper.wrapDetails(customerService.readCustomerByEmail(emailId), null);
            return ResponseEntity.ok(customerWrapper);
        } catch (Exception e) {
            LOGGER.error("Error reading customer by email", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}