package com.anbu.aipos.adapters.in.web.dto.order;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryResponse(
        Long id,
        BigDecimal totalAmount,
        String status,
        Instant createdAt,
        String createdByUsername
) {}