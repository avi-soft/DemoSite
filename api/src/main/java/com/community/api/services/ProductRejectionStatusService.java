package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomProductRejectionStatus;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
public class ProductRejectionStatusService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomProductRejectionStatus> getAllRejectionStatus() throws Exception {
        try {
            List<CustomProductRejectionStatus> rejectionStausList = entityManager.createQuery(Constant.GET_ALL_REJECTION_STATUS, CustomProductRejectionStatus.class).getResultList();
            return rejectionStausList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }

    public CustomProductRejectionStatus getAllRejectionStatusByRejectionStatusId(Long rejectionStatusId) throws Exception {
        try {

            Query query = entityManager.createQuery(Constant.GET_JOB_GROUP_BY_ID, CustomProductRejectionStatus.class);
            query.setParameter("rejectionStatusId", rejectionStatusId);
            List<CustomProductRejectionStatus> rejectionStaus = query.getResultList();

            if (!rejectionStaus.isEmpty()) {
                return rejectionStaus.get(0);
            }
            throw new IllegalArgumentException("NO REJECTION STATE IS FOUND WITH THIS ID");

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception(exception.getMessage());
        }
    }
}
