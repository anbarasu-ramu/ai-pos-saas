package com.anbu.aipos.adapters.in.web.dto.user;

public record CreateUserRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String role
) {}