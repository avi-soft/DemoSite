package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="custom_notifying_authority")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomNotifyingAuthority {

    @Id
    @Column(name="notifying_authority_id")
    protected Long notifying_authority_id;

    @Column(name="notifying_authority")
    protected String notifying_authority;

}
