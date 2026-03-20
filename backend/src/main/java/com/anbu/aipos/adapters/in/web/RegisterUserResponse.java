package com.anbu.aipos.adapters.in.web;

public record RegisterUserResponse(
        String message,
        String email,
        String tenantId,
        String role,
        String loginUrl) {
}
