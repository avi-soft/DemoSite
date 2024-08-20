package com.community.api.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "custom_product_reserve_category_fee_post_reference")
public class ProductReserveCategoryFeePostRef {
    @Column(name = "productId")
    Long productId;

    @Column(name = "reserve_category_id")
    Long reserveCategoryId;

    @Column(name = "fees")
    Double Fees;

    @Column(name = "post")
    Long post;
}
