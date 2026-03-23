package com.anbu.aipos.core.port.in.product;

import java.math.BigDecimal;

public record ProductView(
        Long id,
        String name,
        String category,
        BigDecimal price,
        Integer stockQuantity,
        Boolean active
) {}