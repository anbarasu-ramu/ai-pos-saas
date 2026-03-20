package com.anbu.aipos.application;

import com.anbu.aipos.adapters.out.KeycloakAdminProperties;
import com.anbu.aipos.core.port.in.RegisterUserUseCase;
import com.anbu.aipos.core.port.out.KeycloakAdminPort;
import com.anbu.aipos.tenant.domain.Tenant;
import com.anbu.aipos.tenant.repository.TenantRepository;
import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private static final String TENANT_ADMIN_ROLE = "TENANT_ADMIN";

    private final TenantRepository tenantRepository;
    private final KeycloakAdminPort keycloakAdminPort;
    private final KeycloakAdminProperties keycloakProperties;

    public RegisterUserService(
            TenantRepository tenantRepository,
            KeycloakAdminPort keycloakAdminPort,
            KeycloakAdminProperties keycloakProperties) {
        this.tenantRepository = tenantRepository;
        this.keycloakAdminPort = keycloakAdminPort;
        this.keycloakProperties = keycloakProperties;
    }

    @Override
    public RegistrationResult register(RegistrationCommand command) {
        validatePassword(command.password());

        String tenantName = command.tenantName().trim();
        UUID tenantId = UUID.randomUUID();
        String slug = slugify(tenantName);

        validateTenantName(tenantName, slug);

        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);
        tenant.setSlug(slug);
        tenant.setName(tenantName);

        Tenant savedTenant = tenantRepository.saveAndFlush(tenant);

        try {
            keycloakAdminPort.createTenantAdmin(new KeycloakAdminPort.TenantAdminRegistration(
                    command.email().trim().toLowerCase(Locale.ROOT),
                    command.password(),
                    tenantId.toString(),
                    TENANT_ADMIN_ROLE));
        } catch (RegistrationException ex) {
            tenantRepository.delete(savedTenant);
            tenantRepository.flush();
            throw ex;
        } catch (RuntimeException ex) {
            tenantRepository.delete(savedTenant);
            tenantRepository.flush();
            throw new RegistrationException("Registration failed. Please try again.", HttpStatus.BAD_GATEWAY, ex);
        }

        return new RegistrationResult(
                "Registration successful. Please log in.",
                command.email().trim().toLowerCase(Locale.ROOT),
                tenantId.toString(),
                TENANT_ADMIN_ROLE,
                buildLoginUrl());
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
