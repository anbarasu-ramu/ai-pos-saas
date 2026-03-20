package com.anbu.aipos.core.port.out;

public interface KeycloakAdminPort {

    void createTenantAdmin(TenantAdminRegistration registration);

    record TenantAdminRegistration(
            String email,
            String password,
            String tenantId,
            String role) {
    }
}
