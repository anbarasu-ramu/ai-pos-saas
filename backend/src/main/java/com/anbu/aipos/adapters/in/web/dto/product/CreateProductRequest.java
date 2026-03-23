package com.anbu.aipos.adapters.in.web.dto.product;

import java.math.BigDecimal;

public record CreateProductRequest(
        String name,
        String category,
        BigDecimal price,
        Integer stockQuantity
) {}

