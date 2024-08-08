package com.community.api.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CategoryDao {
    String categoryName;
    Long categoryId;
    Long totalProducts;
    List<CustomProductWrapper> products;

}
