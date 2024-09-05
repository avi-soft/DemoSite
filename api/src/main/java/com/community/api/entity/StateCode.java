package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "custom_state_codes")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StateCode {
    @Id
    private int state_id;
    private String state_name;
    private String state_code;
}
