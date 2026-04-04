package com.anbu.aipos.adapters.in.web.dto.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        Long id,
        BigDecimal totalAmount,
        String status,
        Instant createdAt,
        String createdByUsername,
        UUID createdByUserId,
        List<OrderDetailItemResponse> items
) {
}
