package com.anbu.aipos.adapters.in.web.dto.order;

import java.math.BigDecimal;

public record OrderDetailItemResponse(
        Long productId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
