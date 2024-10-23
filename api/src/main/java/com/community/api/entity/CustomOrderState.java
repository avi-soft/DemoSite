package com.community.api.entity;

import io.swagger.models.auth.In;
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
    @Column(name = "order_state_id")
    private Integer orderStateId;
    @Column(name = "order_status_id")
    private Integer orderStatusId;
    @Id
    @Column(name = "order_id")
    private Long orderId;
    public CustomOrderState(Integer orderStateId) {
        this.orderStateId=orderStateId;
    }
}
