package com.anbu.aipos.inventory.domain;

import com.anbu.aipos.common.domain.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stock_ledger_entry")
public class StockLedgerEntry extends TenantScopedEntity {

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantityDelta;

    @Column(nullable = false, length = 32)
    private String reason;
}
