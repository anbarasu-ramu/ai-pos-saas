package com.anbu.aipos.application.user;

import com.anbu.aipos.adapters.in.web.dto.user.CreateUserRequest;
import com.anbu.aipos.application.exception.RegistrationException;
import com.anbu.aipos.core.port.out.KeycloakAdminPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final KeycloakAdminPort keycloakAdminPort;
//    private final StaffUserRepository staffUserRepository;
    private final StaffUserService staffUserService;

    public UserService(KeycloakAdminPort keycloakAdminPort, StaffUserService staffUserService){
        this.keycloakAdminPort = keycloakAdminPort;

        this.staffUserService = staffUserService;
    }

    public void createUser(CreateUserRequest request, String tenantId) {

        validate(request);

        // default role = CASHIER if not provided
        String role = (request.role() == null || request.role().isBlank())
                ? "CASHIER"
                : request.role();
        String userId = null;
        try {
            KeycloakAdminPort.UserProvisioningRequest registration = new KeycloakAdminPort.UserProvisioningRequest(
                    request.email(),
                    request.password(),
                    tenantId,
                    role
            );
            userId = keycloakAdminPort.createUser(registration);
            this.staffUserService.createStaffUser(request.email(), userId, UUID.fromString(tenantId),role);

        }catch (Exception ex) {

            rollback(ex, userId);
        }


    }

    private void rollback(Exception ex, String userId) {
        // 🔥 rollback Keycloak user
        if (userId != null) {
            try {
                keycloakAdminPort.deleteUser(userId);
            } catch (Exception rollbackEx) {
                log.error("Failed to rollback Keycloak user {}", userId, rollbackEx);
            }
        }


        if (ex instanceof RegistrationException re) {
            throw re;
        }

        throw new RegistrationException(
                "Registration failed. Please try again.",
                HttpStatus.BAD_GATEWAY,
                ex);
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
