package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

import static com.community.api.component.Constant.GET_ALL_APPLICATION_SCOPE;
import static com.community.api.component.Constant.GET_APPLICATION_SCOPE_BY_ID;

@Service
public class ApplicationScopeService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomApplicationScope> getAllApplicationScope() throws Exception {
        try {
            List<CustomApplicationScope> applicationScopeList = entityManager.createNativeQuery(GET_ALL_APPLICATION_SCOPE, CustomApplicationScope.class).getResultList();
            if (!applicationScopeList.isEmpty()) {
                return applicationScopeList;
            } else {
                throw new NoResultException("No application scope found.");
            }
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught in fetching getApplicationScopeById " + exception.getMessage() + "\n");
        }
    }

    public CustomApplicationScope getApplicationScopeById(Long applicationScopeId) throws Exception {
        try {

            Query query = entityManager.createQuery(GET_APPLICATION_SCOPE_BY_ID, CustomApplicationScope.class);
            query.setParameter("applicationScopeId", applicationScopeId);
            List<CustomApplicationScope> applicationScope = query.getResultList();

            if (!applicationScope.isEmpty()) {
                return applicationScope.get(0);
            } else {
                throw new NoResultException("No application scope found with this id.");
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught while fetching application scope " + exception.getMessage() + "\n");
        }
    }
}
