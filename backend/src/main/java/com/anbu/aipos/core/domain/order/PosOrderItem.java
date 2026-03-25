package com.anbu.aipos.core.domain.order;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)  // ✅ important
@AllArgsConstructor
public class PosOrderItem {
    private Long id;
    private String name;
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;

    public static PosOrderItem restore(Long orderId, String name, Long productId, Integer quantity, BigDecimal unitPrice) {
        PosOrderItem posOrderItem = new PosOrderItem();
        posOrderItem.orderId = orderId;
        posOrderItem.productId = productId;
        posOrderItem.name = name;
        posOrderItem.quantity = quantity;
        posOrderItem.unitPrice = unitPrice;
        return posOrderItem;
    }
}