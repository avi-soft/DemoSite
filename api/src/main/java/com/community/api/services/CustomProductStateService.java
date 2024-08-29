package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomApplicationScope;
import com.community.api.entity.CustomJobGroup;
import com.community.api.entity.CustomProductState;
import com.community.api.entity.CustomReserveCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Component
public class CustomProductStateService implements CommandLineRunner {

    @Autowired
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Check if data already exists to avoid duplication
        if (entityManager.createQuery("SELECT COUNT(c) FROM CustomProductState c", Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomProductState(1L, "NEW"));
            entityManager.persist(new CustomProductState(2L, "APPROVED"));
            entityManager.persist(new CustomProductState(3L, "LIVE"));
            entityManager.persist(new CustomProductState(4L, "EXPIRED"));
            entityManager.persist(new CustomProductState(5L, "REJECTED"));
        }

        if (entityManager.createQuery("SELECT COUNT(c) FROM CustomApplicationScope c", Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomApplicationScope(1L, "STATE"));
            entityManager.persist(new CustomApplicationScope(2L, "GOVERNMENT"));
        }

        if (entityManager.createQuery("SELECT COUNT(c) FROM CustomReserveCategory c", Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomReserveCategory(1L, "GEN", "General", true));
            entityManager.persist(new CustomReserveCategory(2L, "SC", "Schedule Caste", false));
            entityManager.persist(new CustomReserveCategory(3L, "ST", "Schedule Tribe", false));
            entityManager.persist(new CustomReserveCategory(4L, "OBC", "Other Backward Caste", false));
        }

        if(entityManager.createQuery(Constant.GET_COUNT_OF_JOB_ROLE, Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomJobGroup(1L, 'A'));
            entityManager.persist(new CustomJobGroup(2L, 'B'));
            entityManager.persist(new CustomJobGroup(3L, 'C'));
            entityManager.persist(new CustomJobGroup(4L, 'D'));
        }
    }
}
