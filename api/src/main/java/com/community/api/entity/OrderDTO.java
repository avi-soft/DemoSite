package com.community.api.entity;
import com.community.api.dto.CustomProductWrapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.core.order.service.type.OrderStatus;

import org.springframework.lang.Nullable;

import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO
{
    private Long orderId;
    private String orderName;
    private Money total;
    private OrderStatus status;
    private Date submitDate;
    private String orderNumber;
    private String customerEmail;
    private Long customerId;
    private Money subTotal;
    private String orderState;
}

