package com.anbu.aipos.adapters.in.web;

import com.anbu.aipos.adapters.out.KeycloakAdminProperties;
import com.anbu.aipos.application.RegistrationException;
import com.anbu.aipos.common.web.ApiResponse;
import com.anbu.aipos.core.port.in.RegisterUserUseCase;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final KeycloakAdminProperties keycloakProperties;

    public AuthController(RegisterUserUseCase registerUserUseCase, KeycloakAdminProperties keycloakProperties) {
        this.registerUserUseCase = registerUserUseCase;
        this.keycloakProperties = keycloakProperties;
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, String>> status() {
        return new ApiResponse<>(
                "Authentication is delegated to Keycloak.",
                Map.of(
                        "provider", "keycloak",
                        "flow", "oidc",
                        "realm", keycloakProperties.getRealm(),
                        "frontendClientId", keycloakProperties.getClientId(),
                        "issuer", keycloakProperties.getPublicBaseUrl() + "/realms/" + keycloakProperties.getRealm()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserRequest request) {
        try {
            RegisterUserUseCase.RegistrationResult result = registerUserUseCase.register(
                    new RegisterUserUseCase.RegistrationCommand(
                            request.email(),
                            request.password(),
                            request.tenantName()));

            return ResponseEntity.ok(new RegisterUserResponse(
                    result.message(),
                    result.email(),
                    result.tenantId(),
                    result.role(),
                    result.loginUrl()));
        } catch (RegistrationException ex) {
            return ResponseEntity.status(ex.getStatus())
                    .body(Map.of("message", ex.getMessage()));
        }
    }
}
