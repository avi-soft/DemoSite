package com.community.api.services;

import com.community.api.entity.CustomProductState;
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
    }
}
