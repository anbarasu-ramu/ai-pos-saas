package com.anbu.aipos.core.port.in.register;

import com.anbu.aipos.adapters.out.persistence.tenant.domain.TenantEntity;

import java.util.UUID;

public interface RegisterUserUseCase {

    RegistrationResult register(RegistrationCommand command);

    record RegistrationCommand(String email, String password, String tenantName) {
    }

    record RegistrationResult(
            String message,
            String email,
            String tenantId,
            String role,
            String loginUrl) {
    }

     record TenantContext(UUID tenantId, TenantEntity savedTenant) {}
}
