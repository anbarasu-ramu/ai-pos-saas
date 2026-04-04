package com.anbu.aipos.ai.application;

import java.util.Map;

public record AiToolInvocation(
        String tool,
        Map<String, Object> arguments,
        String status,
        String errorMessage
) {
}
