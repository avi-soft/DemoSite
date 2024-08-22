package com.community.api.endpoint.avisoft.controller.ServiceProviderAddress;

import com.community.api.component.Constant;
import com.community.api.entity.ServiceProviderAddressRef;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Map;

@RestController
@RequestMapping("/service-provider-address-type")
public class ServiceProviderAddressTypeController {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @GetMapping("/getAddressTypes")
    public ResponseEntity<?> getAddressTypes()
    {
        TypedQuery<ServiceProviderAddressRef> query = entityManager.createQuery(Constant.jpql, ServiceProviderAddressRef.class);
        return new ResponseEntity<>(query.getResultList(), HttpStatus.OK);
    }
    @Transactional
    @PostMapping("/addAddressType")
    public ResponseEntity<?> addAddressType(@RequestBody Map<String,Object> details)
    {
       String address_name=(String) details.get("address_name");
       if(address_name==null)
           return new ResponseEntity<>("Address name cannot be null",HttpStatus.BAD_REQUEST);
       ServiceProviderAddressRef addressRef=new ServiceProviderAddressRef();
       addressRef.setAddress_name(address_name);
       entityManager.persist(addressRef);
       return new ResponseEntity<>(addressRef,HttpStatus.OK);
    }
}
