package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomNotifyingAuthority;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class NotifyingAuthorityService {

    @PersistenceContext
    protected EntityManager entityManager;

    public List<CustomNotifyingAuthority> getAllNotifyingAuthority(){
        try{
            List<CustomNotifyingAuthority> authorities = entityManager.createNativeQuery(Constant.GET_ALL_NOTIFYING_AUTHORITY, CustomNotifyingAuthority.class).getResultList();
        } catch(Exception exception) {



        }
        return authorities;
    }
}
