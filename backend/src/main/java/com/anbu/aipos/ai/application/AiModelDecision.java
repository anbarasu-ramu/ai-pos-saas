package com.anbu.aipos.ai.application;

import java.util.List;

public record AiModelDecision(
        String assistantMessage,
        String intent,
        List<AiToolCall> toolCalls,
        boolean requiresConfirmation
) {
}
