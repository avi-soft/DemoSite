package com.community.api.endpoint.avisoft.custom;

import org.broadleafcommerce.core.catalog.domain.Product;

import java.util.List;

public class CustomCategoryDao {
    String categoryName;
    Long categoryId;
    List<Product> products;

    public String getCategoryName() {
        return this.categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<Product> getProducts() {
        return this.products;
    }

  void setProducts(List<Product> products) {
        this.products = products;
    }
}
