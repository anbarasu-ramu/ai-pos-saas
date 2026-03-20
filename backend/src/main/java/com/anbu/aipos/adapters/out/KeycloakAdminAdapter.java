package com.anbu.aipos.adapters.out;

import com.anbu.aipos.application.RegistrationException;
import com.anbu.aipos.core.port.out.KeycloakAdminPort;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class KeycloakAdminAdapter implements KeycloakAdminPort {

    private final RestClient restClient;
    private final KeycloakAdminProperties properties;

    public KeycloakAdminAdapter(KeycloakAdminProperties properties) {
        this.restClient = RestClient.builder().build();
        this.properties = properties;
    }

    @Override
    public void createTenantAdmin(TenantAdminRegistration registration) {
        String accessToken = null;
        String userId = null;

        try {
            accessToken = fetchAccessToken();
            userId = createUser(accessToken, registration);
            setPassword(accessToken, userId, registration.password());
            assignRealmRole(accessToken, userId, registration.role());
        } catch (RestClientResponseException ex) {
            rollbackUser(accessToken, userId);

            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new RegistrationException("A user with that email already exists.", HttpStatus.CONFLICT, ex);
            }

            throw new RegistrationException("Unable to provision the tenant administrator.", HttpStatus.BAD_GATEWAY, ex);
        } catch (RuntimeException ex) {
            rollbackUser(accessToken, userId);
            throw new RegistrationException("Unable to provision the tenant administrator.", HttpStatus.BAD_GATEWAY, ex);
        }
    }

    private String fetchAccessToken() {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "client_credentials");
        requestBody.add("client_id", properties.getAdminClientId());
        requestBody.add("client_secret", properties.getAdminClientSecret());

        TokenResponse response = restClient.post()
                .uri(UriComponentsBuilder
                        .fromHttpUrl(properties.getAdminBaseUrl())
                        .pathSegment("realms", properties.getRealm(), "protocol", "openid-connect", "token")
                        .build()
                        .toUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(requestBody)
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new RegistrationException("Unable to obtain a Keycloak admin access token.", HttpStatus.BAD_GATEWAY);
        }

        return response.accessToken();
    }

    private String createUser(String accessToken, TenantAdminRegistration registration) {
        Map<String, Object> userPayload = Map.of(
                "username", registration.email(),
                "email", registration.email(),
                "enabled", true,
                "emailVerified", true,
                "attributes", Map.of("tenant_id", List.of(registration.tenantId())));

        URI location = restClient.post()
                .uri(adminUri("users"))
                .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(userPayload)
                .retrieve()
                .toBodilessEntity()
                .getHeaders()
                .getLocation();

        if (location == null) {
            throw new RegistrationException("Keycloak did not return a created user identifier.", HttpStatus.BAD_GATEWAY);
        }

        String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private void setPassword(String accessToken, String userId, String password) {
        Map<String, Object> credential = Map.of(
                "type", "password",
                "temporary", false,
                "value", password);

        restClient.put()
                .uri(adminUri("users/" + userId + "/reset-password"))
                .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(credential)
                .retrieve()
                .toBodilessEntity();
    }

    private void assignRealmRole(String accessToken, String userId, String roleName) {
        Map<?, ?> roleRepresentation = restClient.get()
                .uri(adminUri("roles/" + roleName))
                .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                .retrieve()
                .body(Map.class);

        if (roleRepresentation == null) {
            throw new RegistrationException("Keycloak role mapping failed.", HttpStatus.BAD_GATEWAY);
        }

        restClient.post()
                .uri(adminUri("users/" + userId + "/role-mappings/realm"))
                .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(roleRepresentation))
                .retrieve()
                .toBodilessEntity();
    }

    private void deleteUser(String accessToken, String userId) {
        restClient.delete()
                .uri(adminUri("users/" + userId))
                .header(HttpHeaders.AUTHORIZATION, bearerToken(accessToken))
                .retrieve()
                .toBodilessEntity();
    }

    private void rollbackUser(String accessToken, String userId) {
        if (accessToken == null || userId == null || userId.isBlank()) {
            return;
        }

        try {
            deleteUser(accessToken, userId);
        } catch (RestClientResponseException rollbackEx) {
            if (rollbackEx.getStatusCode() != HttpStatus.NOT_FOUND) {
                throw new RegistrationException(
                        "Registration failed after partially provisioning the tenant administrator.",
                        HttpStatus.BAD_GATEWAY,
                        rollbackEx);
            }
        }
    }

    private URI adminUri(String path) {
        return UriComponentsBuilder
                .fromHttpUrl(properties.getAdminBaseUrl())
                .pathSegment("admin", "realms", properties.getRealm())
                .path(path.startsWith("/") ? path : "/" + path)
                .build()
                .toUri();
    }

    private String bearerToken(String accessToken) {
        return "Bearer " + accessToken;
    }

    private record TokenResponse(String access_token) {
        String accessToken() {
            return access_token;
        }
    }
}
