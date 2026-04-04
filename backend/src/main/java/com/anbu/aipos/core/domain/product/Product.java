package com.anbu.aipos.core.domain.product;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
//@Setter
public class Product {
    private final ProductId id;
    private String name;
    private String category;
    private BigDecimal price;
    private int stockQuantity;
    private boolean active;
    private final String tenantId;

    // 🔒 Constructor (enforce invariants)
    public Product(ProductId id,
                   String name,
                   String category,
                   BigDecimal price,
                   Integer stockQuantity,
                   Boolean active,
                   String tenantId) {
        validateName(name);
        validatePrice(price);
        validateStock(stockQuantity);
        validateTenant(tenantId);
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.active = active;
        this.tenantId = tenantId;
    }

    // 🚀 Factory method (recommended for creation)
    public static Product create(String name,
                                 String category,
                                 BigDecimal price,
                                 int initialStock,
                                 String tenantId) {
        return new Product(
                null,
                name,
                category,
                price,
                initialStock,
                true,
                tenantId
        );
    }

    // ================================
    // 🧠 BUSINESS BEHAVIOR
    // ================================

    public void updateName(String newName) {
        validateName(newName);
        this.name = newName;
    }

    public void updateCategory(String newCategory) {
        validateName(newCategory);
        this.category = newCategory;
    }

    public void updatePrice(BigDecimal newPrice) {
        validatePrice(newPrice);
        this.price = newPrice;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Stock increase must be positive");
        }
        this.stockQuantity += quantity;
    }

    public void reduceStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Stock reduction must be positive");
        }
        if (quantity > stockQuantity) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.stockQuantity -= quantity;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    // ================================
    // 🔒 VALIDATIONS (INVARIANTS)
    // ================================

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (name.length() > 160) {
            throw new IllegalArgumentException("Product name too long");
        }
    }

    private void validateCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
        if (category.length() > 160) {
            throw new IllegalArgumentException("Category name too long");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }

    private void validateStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
    }

    private void validateTenant(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("TenantId is required");
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}