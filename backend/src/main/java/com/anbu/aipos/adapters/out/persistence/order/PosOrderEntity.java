package com.anbu.aipos.adapters.out.persistence.order;

import com.anbu.aipos.common.domain.TenantScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // ✅ important
@AllArgsConstructor
@Table(name = "pos_order")
public class PosOrderEntity extends TenantScopedEntity {


    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 32)
    private String status = "DRAFT";
}
