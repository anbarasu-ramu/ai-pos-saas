package com.anbu.aipos.adapters.in.web.dto.order.checkout;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutApiResponse(
        Long orderId,
        BigDecimal totalAmount,
        BigDecimal change,
        String status,
        List<CheckoutItem> items
) {};

 record CheckoutItem(
        String name,
        int quantity,
        BigDecimal price

){};


