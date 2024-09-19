package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "custom_product_reserve_category_born_before_after_reference")
@AllArgsConstructor
@NoArgsConstructor
@Data
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
