package com.anbu.aipos.core.port.in.product;

import java.math.BigDecimal;

public record CreateProductCommand(
        String name,
        String category,
        BigDecimal price,
        Integer stockQuantity,
        String tenantId
) {}
