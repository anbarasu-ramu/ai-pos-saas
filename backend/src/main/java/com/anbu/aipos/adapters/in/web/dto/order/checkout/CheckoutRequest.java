package com.anbu.aipos.adapters.in.web.dto.order.checkout;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CheckoutRequest(
        List<Item> items,
        String paymentType,
        BigDecimal amountPaid,
        UUID createdByUserId,
        String createdByUsername

) {
    public record Item(
            Long productId,
            int quantity
    ) {}
}


