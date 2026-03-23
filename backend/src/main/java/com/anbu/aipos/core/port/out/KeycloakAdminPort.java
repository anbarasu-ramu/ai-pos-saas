package com.anbu.aipos.core.port.out;

public interface KeycloakAdminPort {

    void createUser(UserProvisioningRequest registration);

    record UserProvisioningRequest(
            String email,
            String password,
            String tenantId,
            String role) {
    }
}
