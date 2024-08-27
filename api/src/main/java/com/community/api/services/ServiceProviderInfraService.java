package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.ServiceProviderInfra;
import com.community.api.entity.Skill;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class ServiceProviderInfraService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Transactional
    public ResponseEntity<?> addInfra(@RequestBody ServiceProviderInfra serviceProviderInfra) {
        try{
            if(serviceProviderInfra.getInfra_name()==null)
                return new ResponseEntity<>("Infra name cannot be empty",HttpStatus.BAD_REQUEST);
            int count=(int)findCount();
            serviceProviderInfra.setInfra_id(++count);
            entityManager.persist(serviceProviderInfra);
            return new ResponseEntity<>(serviceProviderInfra,HttpStatus.OK);
        }catch (Exception exception)
        {
            exceptionHandling.handleException(exception);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error saving skill : " + exception.getMessage());
        }
    }
    public long findCount() {
        String queryString = Constant.GET_INFRA_COUNT;
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult();
    }
    public List<ServiceProviderInfra> findAllInfraList() {
        TypedQuery<ServiceProviderInfra> query = entityManager.createQuery(Constant.GET_INFRA_LIST, ServiceProviderInfra.class);
        return query.getResultList();
    }
}