package com.anbu.aipos.core.port.in;

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
}
