package com.anbu.aipos.adapters.in.web.user;

public record RegisterUserResponse(
        String message,
        String email,
        String tenantId,
        String role,
        String loginUrl) {
}
