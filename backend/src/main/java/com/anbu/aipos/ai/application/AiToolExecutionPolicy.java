package com.anbu.aipos.ai.application;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiToolExecutionPolicy {

    public boolean requiresConfirmation(AiModelDecision decision, String userMessage) {
        if (decision.requiresConfirmation()) {
            return true;
        }

        return decision.toolCalls().stream()
                .map(AiToolCall::tool)
                .anyMatch(tool -> !tool.isReadOnly() && !hasExplicitConfirmation(userMessage));
    }

    public List<AiToolInvocation> confirmationRequiredInvocations(List<AiToolCall> toolCalls) {
        return toolCalls.stream()
                .map(toolCall -> new AiToolInvocation(
                        toolCall.tool().name(),
                        toolCall.arguments(),
                        "CONFIRMATION_REQUIRED",
                        "Explicit confirmation is required before executing this action."))
                .toList();
    }

    boolean hasExplicitConfirmation(String userMessage) {
        String normalized = userMessage == null ? "" : userMessage.toLowerCase();
        return normalized.contains("confirm")
                || normalized.contains("go ahead")
                || normalized.contains("proceed")
                || normalized.contains("place the order")
                || normalized.contains("do it");
    }
}
