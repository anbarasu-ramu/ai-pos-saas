package com.anbu.aipos.adapters.in.web;

import com.anbu.aipos.adapters.in.web.dto.user.RegisterUserRequest;
import com.anbu.aipos.adapters.out.keycloak.KeycloakAdminProperties;
import com.anbu.aipos.application.exception.RegistrationException;
import com.anbu.aipos.application.tenant.TenantQueryService;
import com.anbu.aipos.common.web.ApiResponse;
import com.anbu.aipos.core.port.in.register.RegisterUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final KeycloakAdminProperties keycloakProperties;
    private final TenantQueryService tenantQueryService;

    public AuthController(RegisterUserUseCase registerUserUseCase, KeycloakAdminProperties keycloakProperties, TenantQueryService tenantQueryService) {
        this.registerUserUseCase = registerUserUseCase;
        this.keycloakProperties = keycloakProperties;
        this.tenantQueryService = tenantQueryService;
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

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        var tenantId = resolveTenantId(jwt);
        return new MeResponse(
                true,
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("preferred_username"),
                extractRoles(jwt),
                tenantId,
                this.tenantQueryService.findNameById(UUID.fromString(tenantId)));
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

    private List<String> extractRoles(Jwt jwt) {
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (!(realmAccess instanceof Map<?, ?> claims)) {
            return List.of();
        }

        Object roles = claims.get("roles");
        if (!(roles instanceof List<?> roleList)) {
            return List.of();
        }

        List<String> resolvedRoles = new ArrayList<>();
        for (Object role : roleList) {
            if (role instanceof String roleName && !roleName.isBlank()) {
                resolvedRoles.add(roleName);
            }
        }

        return List.copyOf(resolvedRoles);
    }

    private String resolveTenantId(Jwt jwt) {
        String tenantId = jwt.getClaimAsString("tenant_id");
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = jwt.getClaimAsString("tenant");
        }
        return tenantId;
    }

    public record MeResponse(
            boolean authenticated,
            String subject,
            String email,
            String username,
            List<String> roles,
            String tenantId,
            String tenantName) {
    }
}
