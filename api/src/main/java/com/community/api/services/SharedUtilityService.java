package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.Skill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SharedUtilityService {
    @Autowired
    private EntityManager entityManager;

    public long findCount(String queryString) {
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult();
    }
    public static String getCurrentTimestamp() {
        // Get the current date and time with timezone
        ZonedDateTime zonedDateTime = ZonedDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSXXX");
        return zonedDateTime.format(formatter);
    }
}

