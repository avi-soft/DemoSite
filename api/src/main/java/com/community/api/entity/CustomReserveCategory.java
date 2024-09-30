package com.community.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("reserve_category_id")
    protected Long reserveCategoryId;

    @Column(name = "reserve_category_name")
    @JsonProperty("reserve_category_name")
    protected String reserveCategoryName;

    @Column(name = "reserve_category_description")
    @JsonProperty("reserve_category_description")
    protected String reserveCategoryDescription;

    @Column(name = "is_default_category")
    @JsonProperty("default_category")
    protected Boolean isReservedCategory;
<<<<<<< HEAD

=======
>>>>>>> fae2c91eb2878955cc31b4ec18dd7ce4c4b41b26
}
