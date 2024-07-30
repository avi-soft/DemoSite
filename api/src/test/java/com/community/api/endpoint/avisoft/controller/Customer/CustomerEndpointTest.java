package com.community.api.endpoint.avisoft.controller.Customer;
import com.community.api.component.Constant;
import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.endpoint.customer.CustomerDTO;
import com.community.api.services.CustomCustomerService;
import com.community.api.services.TwilioService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mozilla.javascript.EcmaError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import javax.persistence.EntityManager;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CustomerEndpointTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @Mock
    private ExceptionHandlingImplement exceptionHandling;

    @Mock
    private EntityManager em;

    @Mock
    private TwilioService twilioService;

    @Mock
    private CustomCustomerService customCustomerService;

    @InjectMocks
    private CustomerEndpoint customerEndpoint;
    private JsonReader jsonReader = new JsonReader();

    private Map<String, Object> dataMap;
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(customerEndpoint).build();
        try {
            dataMap = jsonReader.readFile("customer");
            System.out.println(dataMap);
            CustomCustomer customCustomer=new CustomCustomer();
            customCustomer.setId(1L);
        }
        catch (Exception e)
        {
            exceptionHandling.handleException(e);
        }
    }

    @Test
    public void testRetrieveCustomerById_Success() throws Exception {
        CustomCustomer customCustomer=new CustomCustomer();
        customCustomer.setId(1L);
        customCustomer.setMobileNumber((String)dataMap.get("mobileNumber"));
        em.persist(customCustomer);
        given(em.find(CustomCustomer.class, (String)dataMap.get("mobileNumber"))).willReturn(customCustomer);
        mockMvc.perform(get("/customer-custom/getCustomer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mobileNumber", is((String)dataMap.get("mobileNumber"))));
    }

    @Test
    public void testRetrieveCustomerById_NotFound() throws Exception {
        given(customerService.readCustomerById(1L)).willReturn(null);

        mockMvc.perform(get("/customer-custom/getCustomer/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Customer with this ID does not exist"));
    }
    @Test
    public void testAddCustomer_InvalidInput() throws Exception {
        CustomCustomer customerDetails = new CustomCustomer();
        customerDetails.setMobileNumber((String)dataMap.get("mobileNumber"));

        given(customCustomerService.validateInput(customerDetails)).willReturn(false);

        Object mobileNumber = dataMap.get("mobileNumber");


        String jsonContent = String.format("{\"mobileNumber\": \"%s\"}", mobileNumber);

// Perform the MockMvc request with the dynamic content
        mockMvc.perform(post("/customer-custom/register")
                        .contentType("application/json")
                        .content(jsonContent))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("One or more inputs invalid"));
    }

    /*@Test
    public void testUpdateCustomer_Success() throws Exception {
        CustomCustomer customerDetails = new CustomCustomer();
        customerDetails.setMobileNumber("0987654321");

        Customer existingCustomer =customerService.createCustomer();
        existingCustomer.setId(1L);

        given(em.find(CustomCustomer.class, 1L)).willReturn(existingCustomer);
        given(customerService.readCustomerById(1L)).willReturn(existingCustomer);

        mockMvc.perform(patch("/customer-custom/update/1")
                        .contentType("application/json")
                        .content("{\"mobileNumber\": \"0987654321\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Customer Updated"));
    }*/

    @Test
    public void testDeleteCustomer_Success() throws Exception {
        CustomCustomer customCustomer=new CustomCustomer();
        customCustomer.setId(1L);
        //long id=customerService.readCustomerById(customer.getId()).getId();
        given(customerService.readCustomerById(1L)).willReturn(customCustomer);
        mockMvc.perform(delete("/customer-custom/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Record Deleted Successfully"));
    }

    @Test
    public void testDeleteCustomer_NotFound() throws Exception {
        given(customerService.readCustomerById(1L)).willReturn(null);

        mockMvc.perform(delete("/customer-custom/delete/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string("No Records found for this ID"));
    }
}
