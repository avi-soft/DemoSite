package com.community.api.services;

import com.community.api.entity.CustomNotifyingAuthority;
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
            entityManager.persist(new CustomProductState(1L, "New"));
            entityManager.persist(new CustomProductState(2L, "Live"));
            entityManager.persist(new CustomProductState(3L, "Expired"));
            entityManager.persist(new CustomProductState(4L, "Rejected"));
        }

        if (entityManager.createQuery("SELECT COUNT(c) FROM CustomNotifyingAuthority c", Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomNotifyingAuthority(1L, "State"));
            entityManager.persist(new CustomNotifyingAuthority(2L, "Government"));
        }

        if (entityManager.createQuery("SELECT COUNT(c) FROM CustomReserveCategory c", Long.class).getSingleResult() == 0) {
            entityManager.persist(new CustomReserveCategory(1L, "GEN", "GENERAL", true));
            entityManager.persist(new CustomReserveCategory(2L, "SC", "Schedule Caste", false));
            entityManager.persist(new CustomReserveCategory(3L, "ST", "Schedule Tribe", false));
            entityManager.persist(new CustomReserveCategory(4L, "OBC", "Other Backward Caste", false));
        }
    }
}
