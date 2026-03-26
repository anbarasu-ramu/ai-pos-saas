package com.anbu.aipos.core.port.in.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CheckoutUseCase {

    CheckoutResponse checkout(CheckoutCommand command);

    record CheckoutCommand(
            List<Item> items,
            String paymentType,
            BigDecimal amountPaid,
            String tenantId,
            UUID createdByUserId,
            String createdByUsername
    ) {}

    record Item(
            Long productId,
            int quantity
    ) {
    }

    record CheckoutResponse(
            Long orderId,
            BigDecimal totalAmount,
            BigDecimal change,
            String status,
            List<CheckoutItem>  items
    ) {}

    record CheckoutItem(
            Long productId,
            String name,
            int quantity,
            BigDecimal price

    ) {
    }
}