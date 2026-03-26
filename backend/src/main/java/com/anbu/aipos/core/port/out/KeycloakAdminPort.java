package com.anbu.aipos.core.port.out;

public interface KeycloakAdminPort {

    String createUser(UserProvisioningRequest registration);
    void deleteUser(String userId);

    record UserProvisioningRequest(
            String email,
            String password,
            String tenantId,
            String role) {
    }
}
