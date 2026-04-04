package com.anbu.aipos.ai.application;

import java.util.List;
import java.util.UUID;

public record AiExecutionContext(
        String tenantId,
        String tenantName,
        String subject,
        String username,
        String email,
        List<String> roles
) {
    public UUID userIdAsUuid() {
        return UUID.fromString(subject);
    }
}
