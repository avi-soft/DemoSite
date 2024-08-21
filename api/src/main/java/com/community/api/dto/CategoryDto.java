package com.community.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CategoryDto {
    String categoryName;
    Long categoryId;
    Long totalProducts;
    List<CustomProductWrapper> products;

}
