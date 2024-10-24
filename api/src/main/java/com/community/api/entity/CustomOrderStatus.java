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
@Table(name = "custom_order_status")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomOrderStatus {
    @Id
    @Column(name = "order_status_id")
    private Integer orderStatusId;
    @Column(name = "order_status_name")
    private String orderStatusName;
    @Column(name = "order_state_id")
    private Integer orderStateId;
    @Column(name = "order_status_description")
    private String orderStatusDescription;
}
