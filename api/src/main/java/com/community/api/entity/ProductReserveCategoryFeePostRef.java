package com.community.api.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "custom_product_reserve_category_fee_post_xref")
public class ProductReserveCategoryFeePostRef {

    @Id
    @Column(name = "product_reserve_category_id")
    Long productReservedCategoryId;

    @Column(name = "product_id")
    Long productId;

    @Column(name = "reserve_category_id")
    Long reserveCategoryId;

    @Column(name = "fee")
    Double fee;

    @Column(name = "post")
    Long post;
}
