package com.anbu.aipos.core.domain.product;

public record ProductId(Long value) {

    public ProductId {
        if (value == null) {
            throw new IllegalArgumentException("ProductId cannot be null");
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}