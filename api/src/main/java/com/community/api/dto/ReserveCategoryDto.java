package com.community.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveCategoryDto {
    @JsonProperty("product_id")
    Long productId;
    @JsonProperty("reserve_category_id")
    Long reserveCategoryId;
    @JsonProperty("fee")
    Double fee;
    @JsonProperty("post")
    Integer post;
    @JsonProperty("reserve_category")
    String reserveCategory;
    @JsonProperty("born_before")
    Date bornBefore;
    @JsonProperty("born_after")
    Date bornAfter;
}
