package com.community.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveCategoryDto {
    Long productId;
    Long reserveCategoryId;
    Double fee;
    Integer post;
    Date bornBefore;
    Date bornAfter;
}
