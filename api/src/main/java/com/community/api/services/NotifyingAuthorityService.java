package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomNotifyingAuthority;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class NotifyingAuthorityService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomNotifyingAuthority> getAllNotifyingAuthority(){
        try{
            List<CustomNotifyingAuthority> authorities = entityManager.createNativeQuery(Constant.GET_ALL_NOTIFYING_AUTHORITY, CustomNotifyingAuthority.class).getResultList();
            return authorities;
        } catch(Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
}
