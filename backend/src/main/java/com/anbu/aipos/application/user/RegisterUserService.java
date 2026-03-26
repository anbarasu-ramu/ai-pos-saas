package com.anbu.aipos.application.user;

import com.anbu.aipos.adapters.out.keycloak.KeycloakAdminProperties;
import com.anbu.aipos.adapters.out.persistence.tenant.domain.TenantEntity;
import com.anbu.aipos.adapters.out.persistence.tenant.repository.TenantJpaRepository;
import com.anbu.aipos.adapters.out.persistence.user.StaffUserRepository;
import com.anbu.aipos.application.exception.RegistrationException;
import com.anbu.aipos.core.port.in.register.RegisterUserUseCase;
import com.anbu.aipos.core.port.out.KeycloakAdminPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
public class RegisterUserService implements RegisterUserUseCase {

    private static final String TENANT_ADMIN_ROLE = "TENANT_ADMIN";

    private final TenantJpaRepository tenantRepository;
    private final KeycloakAdminPort keycloakAdminPort;
    private final KeycloakAdminProperties keycloakProperties;
    private final StaffUserRepository staffUserRepository;
    private final StaffUserService staffUserService;

    public RegisterUserService(
            TenantJpaRepository tenantRepository,
            KeycloakAdminPort keycloakAdminPort,
            KeycloakAdminProperties keycloakProperties, StaffUserRepository staffUserRepository, StaffUserService staffUserService) {
        this.tenantRepository = tenantRepository;
        this.keycloakAdminPort = keycloakAdminPort;
        this.keycloakProperties = keycloakProperties;
        this.staffUserRepository = staffUserRepository;
        this.staffUserService = staffUserService;
    }

    @Override
    public RegistrationResult register(RegistrationCommand command) {
        validatePassword(command.password());
        String userId = null;
        var tenantContext = createTenant(command);

        try {
            userId = createKeycloakUser(command, tenantContext);
            this.staffUserService.createStaffUser(command.email(), userId, tenantContext.tenantId(),TENANT_ADMIN_ROLE);
        } catch (Exception ex) {
            rollback(ex, userId, tenantContext);
        }

        return new RegistrationResult(
                "Registration successful. Please log in.",
                command.email().trim().toLowerCase(Locale.ROOT),
                tenantContext.tenantId().toString(),
                TENANT_ADMIN_ROLE,
                buildLoginUrl());
    }

    private void rollback(Exception ex, String userId, TenantContext tenantContext) {
        // 🔥 rollback Keycloak user
        if (userId != null) {
            try {
                keycloakAdminPort.deleteUser(userId);
            } catch (Exception rollbackEx) {
                log.error("Failed to rollback Keycloak user {}", userId, rollbackEx);
            }
        }

        // 🔥 rollback tenant
        tenantRepository.delete(tenantContext.savedTenant());
        tenantRepository.flush();

        if (ex instanceof RegistrationException re) {
            throw re;
        }

        throw new RegistrationException(
                "Registration failed. Please try again.",
                HttpStatus.BAD_GATEWAY,
                ex);
    }



    private String createKeycloakUser(RegistrationCommand command, TenantContext tenantContext) {
        String userId;
        userId =  keycloakAdminPort.createUser(new KeycloakAdminPort.UserProvisioningRequest(
                command.email().trim().toLowerCase(Locale.ROOT),
                command.password(),
                        tenantContext.tenantId().toString(),
                TENANT_ADMIN_ROLE));
        return userId;
    }


    private TenantContext createTenant(RegistrationCommand command) {

        String tenantName = command.tenantName().trim();
        UUID tenantId = UUID.randomUUID();
        String slug = slugify(tenantName);
        String userId = null;

        validateTenantName(tenantName, slug);

        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId(tenantId);
        tenant.setSlug(slug);
        tenant.setName(tenantName);

        TenantEntity savedTenant = tenantRepository.saveAndFlush(tenant);

        return new TenantContext(tenantId, savedTenant);
    }

    private String buildLoginUrl() {
        return UriComponentsBuilder
                .fromHttpUrl(keycloakProperties.getPublicBaseUrl())
                .pathSegment("realms", keycloakProperties.getRealm(), "protocol", "openid-connect", "auth")
                .queryParam("client_id", keycloakProperties.getClientId())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid profile email")
                .queryParam("redirect_uri", keycloakProperties.getRedirectUri())
                .build()
                .toUriString();
    }

    private void validatePassword(String password) {
        if (password == null
                || password.length() < 8
                || !password.chars().anyMatch(Character::isUpperCase)
                || !password.chars().anyMatch(Character::isLowerCase)
                || !password.chars().anyMatch(Character::isDigit)) {
            throw new RegistrationException(
                    "Password must be at least 8 characters and include upper, lower, and numeric characters.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private void validateTenantName(String tenantName, String slug) {
        if (tenantRepository.existsByNameIgnoreCase(tenantName) || tenantRepository.existsBySlug(slug)) {
            throw new RegistrationException(
                    "A store with that name already exists. Please choose a different store name.",
                    HttpStatus.CONFLICT);
        }
    }

    private String slugify(String tenantName) {
        String normalized = Normalizer.normalize(tenantName == null ? "" : tenantName.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        if (normalized.isBlank()) {
            return "tenant";
        }

        return normalized.substring(0, Math.min(normalized.length(), 50));
    }
}
