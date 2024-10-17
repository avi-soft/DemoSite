package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderImpl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ORDER_STATE")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomOrderState {
    @Column(name = "order_state")
    private String orderState;
    @Column(name = "order_state_description")
    private String orderStateDescription;
    @Id
    @Column(name = "order_id")
    private Long orderId;
    public CustomOrderState(String orderState, String orderStateDescription) {
        this.orderState=orderState;
        this.orderStateDescription=orderStateDescription;
    }
}
