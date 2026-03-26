package com.anbu.aipos.core.domain.order;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
//@Setter
//@Builder
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)  // ✅ important
@AllArgsConstructor
public class PosOrder {
    private Long id;
    private final List<PosOrderItem> items = new ArrayList<>();
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String status;
    private String tenantId;
    private UUID createdByUserId;
    private String createdByUsername;

    // 🔥 Create order
    public static PosOrder create(String tenantId,UUID createdByUserId, String createdByUsername) {
        PosOrder order = new PosOrder();
        order.tenantId = tenantId;
        order.createdByUserId = createdByUserId;
        order.createdByUsername = createdByUsername;
        order.status = "CREATED";
        return order;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    // 🔥 Add item (core business logic)
    public void addItem(Long productId, String name, int quantity, BigDecimal unitPrice) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }

        PosOrderItem item = new PosOrderItem(
                null,
                name,
                null,
                productId,
                quantity,
                unitPrice
        );

        items.add(item);

        BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        totalAmount = totalAmount.add(itemTotal);
    }

    // 🔥 Complete order
    public void complete(String paymentType, BigDecimal amountPaid) {
        if (items.isEmpty()) {
            throw new IllegalStateException("Order cannot be empty");
        }

        log.info("Amount Paid: "+ amountPaid + ", Total Amount: "+ totalAmount);
        if (amountPaid.compareTo(totalAmount) < 0) {
            throw new RuntimeException("Insufficient payment");
        }

        this.status = "COMPLETED";
    }

    public static PosOrder restore(long id, BigDecimal totalAmount, String status) {
        PosOrder order = new PosOrder();
        order.id = id;
        order.status = status;
        order.totalAmount = totalAmount;
        return order;
    }

}