package com.anbu.aipos.adapters.out.persistence.tenant.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

//@Service
public class TenantContextService {

    public Optional<String> currentTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return Optional.empty();
        }

        String tenantId = jwt.getClaimAsString("tenant_id");
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = jwt.getClaimAsString("tenant");
        }

        return Optional.ofNullable(tenantId).filter(value -> !value.isBlank());
    }
}
