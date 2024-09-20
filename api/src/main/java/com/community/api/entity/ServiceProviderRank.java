package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "service_provider_rank")
public class ServiceProviderRank
{
    @Column(name = "rank_id")
    @Id
    private Long rank_id;
    @Column(name = "rank_name")
    private String  rank_name;
    @Column(name = "rank_description")
    private String rank_description;
    private String created_at,updated_at,created_by;
}
