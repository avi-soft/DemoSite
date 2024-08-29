package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomJobGroup;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
public class JobGroupService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomJobGroup> getAllJobGroup() {
        try {
            List<CustomJobGroup> jobGroupList = entityManager.createQuery(Constant.GET_ALL_JOB_GROUP, CustomJobGroup.class).getResultList();
            return jobGroupList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    public CustomJobGroup getJobGroupById(Long jobGroupId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_JOB_GROUP_BY_ID, CustomJobGroup.class);
            query.setParameter("jobGroupId", jobGroupId);
            List<CustomJobGroup> jobGroup = query.getResultList();

            if (!jobGroup.isEmpty()) {
                return jobGroup.get(0);
            } else {
                return null;
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

}
