package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.broadleafcommerce.common.money.Money;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "custom_product_reserve_category_fee_post_reference")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomProductReserveCategoryFeePostRef {

    @Id
    @Column(name = "product_reserve_category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long productReservedCategoryId;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "product_id")
    protected CustomProduct customProduct;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "reserve_category_id")
    protected CustomReserveCategory customReserveCategory;

    @Column(name = "fee")
    Double fee;

    @Column(name = "post")
    Integer post;
}
