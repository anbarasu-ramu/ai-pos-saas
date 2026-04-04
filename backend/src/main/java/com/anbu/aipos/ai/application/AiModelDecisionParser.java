package com.anbu.aipos.ai.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AiModelDecisionParser {

    private final ObjectMapper objectMapper;

    public AiModelDecisionParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AiModelDecision parse(String rawResponse) {
        String sanitized = sanitize(rawResponse);
        try {
            RawDecision rawDecision = objectMapper.readValue(sanitized, RawDecision.class);
            if (rawDecision.intent() == null || rawDecision.intent().isBlank()) {
                throw new AiInvalidModelResponseException("Model response did not include an intent.");
            }

            List<AiToolCall> toolCalls = rawDecision.toolCalls() == null
                    ? List.of()
                    : rawDecision.toolCalls().stream()
                    .map(this::toToolCall)
                    .toList();

            return new AiModelDecision(
                    rawDecision.assistantMessage(),
                    rawDecision.intent().trim(),
                    toolCalls,
                    Boolean.TRUE.equals(rawDecision.requiresConfirmation()));
        } catch (IllegalArgumentException | JsonProcessingException ex) {
            throw new AiInvalidModelResponseException("Unable to parse model decision.", ex);
        }
    }

    String sanitize(String rawResponse) {
        if (rawResponse == null) {
            throw new AiInvalidModelResponseException("Model response was empty.");
        }

        String sanitized = rawResponse.trim();
        if (sanitized.startsWith("```")) {
            sanitized = sanitized.replaceFirst("^```(?:json)?\\s*", "");
            sanitized = sanitized.replaceFirst("\\s*```$", "");
        }

        return sanitized.trim();
    }

    private AiToolCall toToolCall(RawToolCall rawToolCall) {
        if (rawToolCall.tool() == null || rawToolCall.tool().isBlank()) {
            throw new AiInvalidModelResponseException("Tool call was missing tool name.");
        }

        return new AiToolCall(
                AiTool.valueOf(rawToolCall.tool().trim()),
                rawToolCall.arguments() == null ? Map.of() : rawToolCall.arguments());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RawDecision(
            String assistantMessage,
            String intent,
            List<RawToolCall> toolCalls,
            Boolean requiresConfirmation
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RawToolCall(
            String tool,
            Map<String, Object> arguments
    ) {
    }
}
