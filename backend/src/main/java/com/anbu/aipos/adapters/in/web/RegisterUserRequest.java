package com.anbu.aipos.adapters.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @Email(message = "A valid email address is required.")
        @NotBlank(message = "Email is required.")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, message = "Password must be at least 8 characters long.")
        String password,

        @NotBlank(message = "Tenant name is required.")
        String tenantName) {
}
