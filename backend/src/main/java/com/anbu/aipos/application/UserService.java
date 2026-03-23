package com.anbu.aipos.application;

import com.anbu.aipos.adapters.in.web.dto.CreateUserRequest;
import com.anbu.aipos.core.port.out.KeycloakAdminPort;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final KeycloakAdminPort keycloakAdminPort;

    public UserService(KeycloakAdminPort keycloakAdminPort){
        this.keycloakAdminPort = keycloakAdminPort;
    }

    public void createUser(CreateUserRequest request, String tenantId) {

        validate(request);

        // default role = CASHIER if not provided
        String role = (request.role() == null || request.role().isBlank())
                ? "CASHIER"
                : request.role();

        KeycloakAdminPort.UserProvisioningRequest registration = new KeycloakAdminPort.UserProvisioningRequest(
                request.email(),
                request.password(),
                tenantId,
                role
        );

        keycloakAdminPort.createUser(registration);
    }

    private void validate(CreateUserRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.password() == null || request.password().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
    }
}
