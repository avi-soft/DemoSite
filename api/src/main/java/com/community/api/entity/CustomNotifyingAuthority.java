package com.community.api.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Data
@Table(name="custom_notifying_authority")
public class CustomNotifyingAuthority {

    @Column(name="notifying_authority_id")
    protected Long notifying_authority_id;

    @Column(name="notifying_authority")
    protected String notifying_authority;

}
