package com.community.api.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "custom_product_reserve_category_fee_post_xref")
public class ProductReserveCategoryFeePostRef {

    @Id
    @Column(name = "product_reserve_category_id")
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
    Long post;
}
