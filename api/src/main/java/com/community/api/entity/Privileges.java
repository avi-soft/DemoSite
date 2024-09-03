package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "custom_privileges")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Privileges {
    @Id
    int privilege_id;
    String privilege_name,description;
}
