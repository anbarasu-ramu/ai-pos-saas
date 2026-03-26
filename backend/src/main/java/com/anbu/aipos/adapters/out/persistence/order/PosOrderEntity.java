package com.anbu.aipos.adapters.out.persistence.order;

import com.anbu.aipos.common.domain.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

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

    // ✅ NEW: track who created the order
    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "created_by_username", length = 100)
    private String createdByUsername;
}
