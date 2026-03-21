package com.anbu.aipos.adapters.in.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        System.out.println("subject " + jwt.getSubject());
        System.out.println("email " + jwt.getClaim("email"));
        System.out.println("username " + jwt.getClaim("preferred_username"));
        System.out.println("tenant_id " + jwt.getClaim("tenant_id"));
        return Map.of(
                "subject", jwt.getSubject(),
                "email", jwt.getClaim("email"),
                "username", jwt.getClaim("preferred_username"),
                "tenant_id", jwt.getClaim("tenant_id"),
                "roles", jwt.getClaim("realm_access")
        );
    }
}
