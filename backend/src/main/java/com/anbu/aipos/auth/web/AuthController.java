package com.anbu.aipos.auth.web;

import com.anbu.aipos.common.web.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/status")
    public ApiResponse<Map<String, String>> status() {
        return new ApiResponse<>(
                "Authentication is delegated to Keycloak.",
                Map.of(
                        "provider", "keycloak",
                        "flow", "oidc",
                        "realm", "ai-pos",
                        "frontendClientId", "pos-frontend",
                        "issuer", "http://localhost:8081/realms/ai-pos"));
    }
}
