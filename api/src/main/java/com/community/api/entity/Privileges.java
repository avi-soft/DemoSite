package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Privileges {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int privilege_id;
    String privilege_name,description;
}
