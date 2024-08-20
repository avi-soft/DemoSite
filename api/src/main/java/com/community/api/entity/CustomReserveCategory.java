package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "custom_reserve_category")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomReserveCategory {

    @Id
    @Column(name = "reserve_category_id")
    protected Long reserveCategoryId;

    @Column(name = "reserve_category_name")
    protected String reserveCategoryName;

    @Column(name = "reserve_category_description")
    protected String reserveCategoryDescription;

    @Column(name = "is_default_category")
    protected boolean isReservedCategory;
}