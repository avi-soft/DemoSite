package com.community.api.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "custom_reserved_category")
public class CustomReserveCategory {

    @Column(name = "reserve_category_id")
    protected Long reserveCategoryId;

    @Column(name = "reserve_category_name")
    protected String reserveCategoryName;

    @Column(name = "reserve_category_description")
    protected String reserveCategoryDescription;

    @Column(name = "is_default_category")
    protected boolean isReservedCategory;
}
