package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomGender;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
public class GenderService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    ExceptionHandlingService exceptionHandlingService;

    public List<CustomGender> getAllGender() {
        try {
            List<CustomGender> customGenderList = entityManager.createQuery(Constant.GET_ALL_GENDER, CustomGender.class).getResultList();
            return customGenderList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }

    public CustomGender getGenderByGenderId(Long genderId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_GENDER_BY_GENDER_ID, CustomGender.class);
            query.setParameter("genderId", genderId);
            List<CustomGender> customGender = query.getResultList();

            if (!customGender.isEmpty()) {
                return customGender.get(0);
            } else {
                return null;
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
}
