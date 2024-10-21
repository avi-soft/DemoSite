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
@Table(name = "order_state_ref")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderStateRef {
    @Id
    @Column(name = "order_state_id")
    private Integer orderStateId;
    @Column(name = "order_state_name")
    private String orderStateName;
    @Column(name = "order_state_description")
    private String  orderStateDescription;
}
