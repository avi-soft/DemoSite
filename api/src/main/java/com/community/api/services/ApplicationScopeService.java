package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class ApplicationScopeService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomApplicationScope> getAllApplicationScope(){
        try{
            List<CustomApplicationScope> applicationScopeList = entityManager.createNativeQuery(Constant.GET_ALL_APPLICATION_SCOPE, CustomApplicationScope.class).getResultList();
            return applicationScopeList;
        } catch(Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
}
