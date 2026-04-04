package com.anbu.aipos.adapters.in.web.dto.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record DailyOrderSummaryResponse(
        LocalDate businessDate,
        Instant rangeStart,
        Instant rangeEnd,
        long totalOrders,
        long completedOrders,
        BigDecimal totalRevenue,
        BigDecimal averageOrderValue
) {
}
