package com.community.api.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "custom_product_reserve_category_born_before_after_reference")
public class CustomProductReserveCategoryBornBeforeAfterRef {

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

    @Column(name = "born_before")
    Date bornBefore;

    @Column(name = "born_after")
    Date bornAfter;
}
