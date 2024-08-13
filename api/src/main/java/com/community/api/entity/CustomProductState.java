package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "custom_product_state")
public class CustomProductState {

    @Id
    @Column(name = "product_state_id")
    Long productStateId;

    @Column(name = "product_state")
    String productState;

}


