package com.anbu.aipos.ai.application;

import java.util.List;

public record AiChatResponse(
        String assistantMessage,
        String intent,
        List<AiToolInvocation> toolInvocations,
        Object result,
        boolean requiresConfirmation
) {
}
