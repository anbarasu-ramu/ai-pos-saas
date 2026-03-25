package com.anbu.aipos.adapters.out.persistence.order;

import com.anbu.aipos.common.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // ✅ important
@AllArgsConstructor
@Entity
@Table(name = "pos_order_item")
public class PosOrderItemEntity extends AuditableEntity {

//    @Column(nullable = false)
//    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PosOrderEntity order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

}
