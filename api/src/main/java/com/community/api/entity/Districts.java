package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "custom_districts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Districts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int district_id;
    private String district_name;
    private String state_code;
}