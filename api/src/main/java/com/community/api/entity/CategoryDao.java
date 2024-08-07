package com.community.api.entity;

import com.broadleafcommerce.rest.api.wrapper.ProductWrapper;
import org.broadleafcommerce.core.catalog.domain.Product;

import java.util.List;

public class CategoryDao {
    String categoryName;
    Long categoryId;
    Long totalProducts;
    List<ProductWrapper> products;

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

    public List<ProductWrapper> getProducts() {
        return this.products;
    }

    public void setProducts(List<ProductWrapper> products) {
        this.products = products;
    }

    public void setTotalProducts(Long totalProducts){ this.totalProducts = totalProducts; }

    public Long getTotalProducts() { return this.totalProducts; }
}
