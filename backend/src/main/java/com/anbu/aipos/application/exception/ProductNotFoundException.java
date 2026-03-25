package com.anbu.aipos.application.exception;


public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
    }

    public ProductNotFoundException(Long productId, String tenantId) {
        super("Product not found with id: " + productId + " for tenant: " + tenantId);
    }
}
