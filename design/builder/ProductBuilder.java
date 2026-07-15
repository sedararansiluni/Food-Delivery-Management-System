package com.example.foodorderingsystem.design.builder;

import com.example.foodorderingsystem.model.Product;

public class ProductBuilder {
    private Product product;

    public ProductBuilder() {
        this.product = new Product();
    }

    public ProductBuilder withName(String name) {
        product.setName(name);
        return this;
    }

    public ProductBuilder withDescription(String description) {
        product.setDescription(description);
        return this;
    }

    public ProductBuilder withPrice(Double price) {
        product.setPrice(price);
        return this;
    }

    public ProductBuilder withCategory(String category) {
        product.setCategory(category);
        return this;
    }

    public ProductBuilder withAvailability(Integer availability) {
        product.setAvailability(availability);
        product.setInStock(availability > 0);
        return this;
    }

    public ProductBuilder withImagePath(String imagePath) {
        product.setImagePath(imagePath);
        return this;
    }

    public Product build() {
        return product;
    }
}
