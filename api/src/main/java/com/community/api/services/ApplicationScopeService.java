package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
public class ApplicationScopeService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomApplicationScope> getAllApplicationScope() {
        try {
            List<CustomApplicationScope> applicationScopeList = entityManager.createNativeQuery(Constant.GET_ALL_APPLICATION_SCOPE, CustomApplicationScope.class).getResultList();
            return applicationScopeList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    public CustomApplicationScope getApplicationScopeById(Long applicationScopeId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_APPLICATION_SCOPE_BY_ID, CustomApplicationScope.class);
            query.setParameter("applicationScopeId", applicationScopeId);
            List<CustomApplicationScope> applicationScope = query.getResultList();

            if (!applicationScope.isEmpty()) {
                return applicationScope.get(0);
            } else {
                return null;
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
}
