package com.community.api.entity;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "custom_skill_set")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Skill
{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "skill_id")
    private int skill_id;
    @Column(name = "skill_Name")
    private String skill_name;
}
