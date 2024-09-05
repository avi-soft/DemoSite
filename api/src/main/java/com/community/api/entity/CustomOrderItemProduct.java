package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.broadleafcommerce.core.order.domain.OrderItemImpl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "custom_order_item_product")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomOrderItemProduct {
    @Id
    private Long id;
    @Column(name="product_id")
    private Long productId;
}