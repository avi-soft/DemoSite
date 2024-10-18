package com.community.api.entity;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.broadleafcommerce.core.order.domain.Order;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "sp_accepted_orders")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServiceProviderAcceptedOrders {

    @GeneratedValue
    @Id
    @Column(name = "sp_order_uid")
    private long serviceProviderOrderId;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY) // Use lazy loading to improve performance if needed
    @JoinColumn(name = "service_provider_id") // Explicitly specify the foreign key column
    @JsonIgnore
    private ServiceProviderEntity serviceProvider;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
