package com.community.api.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "custom_product_reserve_category_born_before_after_reference")
public class ProductReserveCategoryBornBeforeAfterRef {

    @Id
    @Column(name = "product_reserve_category_id")
    Long productReservedCategoryId;

    @Column(name = "product_id")
    Long productId;

    @Column(name = "reserve_category_id")
    Long reserveCategoryId;

    @Column(name = "born_before")
    Date bornBefore;

    @Column(name = "born_after")
    Date bornAfter;
}
