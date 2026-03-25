package com.anbu.aipos.adapters.out.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.keycloak")
public class KeycloakAdminProperties {

    private String publicBaseUrl = "http://localhost:8081";
    private String adminBaseUrl = "http://localhost:8081";
    private String realm = "ai-pos";
    private String clientId = "pos-client";
    private String redirectUri = "http://localhost:4200";
    private String adminClientId = "pos-admin-service";
    private String adminClientSecret = "pos-admin-service-secret";

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public String getAdminBaseUrl() {
        return adminBaseUrl;
    }

    public void setAdminBaseUrl(String adminBaseUrl) {
        this.adminBaseUrl = adminBaseUrl;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getAdminClientId() {
        return adminClientId;
    }

    public void setAdminClientId(String adminClientId) {
        this.adminClientId = adminClientId;
    }

    public String getAdminClientSecret() {
        return adminClientSecret;
    }

    public void setAdminClientSecret(String adminClientSecret) {
        this.adminClientSecret = adminClientSecret;
    }
}
