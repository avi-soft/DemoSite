package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("application_scope_id")
    protected Long applicationScopeId;

    @Column(name="application_scope")
    @JsonProperty("application_scope")
    protected String applicationScope;

    @Column(name="application_scope_description")
    @JsonProperty("application_scope_description")
    protected String applicationScopeDescription;

}
