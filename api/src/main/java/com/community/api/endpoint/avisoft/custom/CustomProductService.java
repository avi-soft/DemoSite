package com.community.api.endpoint.avisoft.custom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnit;
import javax.transaction.Transactional;

@Repository
public class CustomProductService {

    @PersistenceUnit
    protected EntityManager em;

    @Transactional
    void save(CustomProduct customProduct){
        System.out.println("Inside CustomProductRepository");
        em.merge(customProduct);
    }
}
