package com.anbu.aipos.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class TenantScopedEntity extends AuditableEntity {

    @Column(nullable = false, length = 64)
    private String tenantId;
}
