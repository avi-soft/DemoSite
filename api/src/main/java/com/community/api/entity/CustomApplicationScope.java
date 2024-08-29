package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="custom_application_scope")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomApplicationScope {

    @Id
    @Column(name="application_scope_id")
    protected Long applicationScopeId;

    @Column(name="application_scope")
    protected String applicationScope;

}
