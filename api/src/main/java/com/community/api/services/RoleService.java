package com.community.api.services;

import com.community.api.component.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;

@Service
public class RoleService {
    @Autowired
    private EntityManager entityManager;
    public String findRoleName(int role_id) {
        return entityManager.createQuery(Constant.FETCH_ROLE, String.class)
                .setParameter("role_id", role_id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}
