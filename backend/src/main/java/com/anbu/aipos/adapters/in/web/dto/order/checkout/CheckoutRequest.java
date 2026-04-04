package com.anbu.aipos.adapters.in.web.dto.order.checkout;
import java.math.BigDecimal;
import java.util.List;

public record CheckoutRequest(
        List<Item> items,
        String paymentType,
        BigDecimal amountPaid

) {
    public record Item(
            Long productId,
            int quantity
    ) {}
}

