package com.community.api.endpoint.avisoft.controller.ServiceProviderAddress;

import com.community.api.component.Constant;
import com.community.api.entity.ServiceProviderAddressRef;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Map;

@RestController
@RequestMapping("/service-provider-address-type")
public class ServiceProviderAddressTypeController {
    @Autowired
    private ResponseService responseService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @GetMapping("/getAddressTypes")

    public ResponseEntity<?> getAddressTypes()
    {
        TypedQuery<ServiceProviderAddressRef> query = entityManager.createQuery(Constant.jpql, ServiceProviderAddressRef.class);
        return responseService.generateSuccessResponse("List fetched successfully",query.getResultList(), HttpStatus.OK);
    }
    @Transactional
    @PostMapping("/add-address-type")
    public ResponseEntity<?> addAddressType(@RequestBody Map<String,Object> details)
    {
        try{
            String address_name=(String) details.get("address_name");
            if(address_name==null)
                return responseService.generateErrorResponse("Address name cannot be null",HttpStatus.BAD_REQUEST);
            ServiceProviderAddressRef addressRef=new ServiceProviderAddressRef();
            addressRef.setAddress_name(address_name);
            entityManager.persist(addressRef);
            return responseService.generateSuccessResponse("Address data list",addressRef ,HttpStatus.OK);

        }catch(Exception e){
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Some error occurred"+ e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
