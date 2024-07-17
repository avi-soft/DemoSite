package com.community.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostgresQueryHelper {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PostgresQueryHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int executeQuery(String query, Object[] params, String operation) {
        switch (operation.toLowerCase()) {
            case "insert":
                return jdbcTemplate.update(query, params);
            case "update":
                return jdbcTemplate.update(query, params);
            case "delete":
                return jdbcTemplate.update(query, params);
            case "select":
                return jdbcTemplate.queryForObject(query, params, Integer.class);
            default:
                throw new IllegalArgumentException("Unsupported operation: " + operation);
        }
    }
}