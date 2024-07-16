package com.community.api.endpoint.avisoft.custom;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class CustomProductRepository {

    @PersistenceContext(unitName="blPU")
    protected EntityManager em;

    void save(CustomProduct customProduct){
        System.out.println("Inside CustomProductRepository");
        em.merge(customProduct);
    }
}
