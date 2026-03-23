package com.anbu.aipos.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

//@Getter
//@Setter
@MappedSuperclass
public abstract class TenantScopedEntity extends AuditableEntity {

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Column(nullable = false, length = 64)
    private String tenantId;


}
