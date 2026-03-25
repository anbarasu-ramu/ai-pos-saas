package com.anbu.aipos.application.tenant;

import com.anbu.aipos.adapters.out.persistence.tenant.repository.TenantJpaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TenantQueryService {

    private final TenantJpaRepository tenantRepo;

    public TenantQueryService(TenantJpaRepository tenantRepo) {
        this.tenantRepo = tenantRepo;
    }

    public String findNameById(UUID tenantId) {
        return tenantRepo.findNameByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    }
}
