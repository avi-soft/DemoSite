package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.StateCode;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class StateCodeService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<StateCode> getAllStates(){
        try{
            List<StateCode> states = entityManager.createNativeQuery(Constant.GET_ALL_STATES, StateCode.class).getResultList();
            return states;
        } catch(Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
}
