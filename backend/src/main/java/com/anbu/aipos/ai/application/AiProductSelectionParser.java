package com.anbu.aipos.ai.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AiProductSelectionParser {

    private final ObjectMapper objectMapper;

    public AiProductSelectionParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AiProductSelectionResult parse(String rawResponse) {
        String sanitized = sanitize(rawResponse);
        try {
            RawSelection rawSelection = objectMapper.readValue(sanitized, RawSelection.class);
            List<AiSemanticProductMatch> matches = rawSelection.matches() == null
                    ? List.of()
                    : rawSelection.matches().stream()
                    .map(this::toMatch)
                    .toList();

            ensureUniqueProductIds(matches);
            return new AiProductSelectionResult(matches);
        } catch (JsonProcessingException ex) {
            throw new AiInvalidModelResponseException("Unable to parse semantic product selection.", ex);
        }
    }

    private String sanitize(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new AiInvalidModelResponseException("Semantic product selection response was empty.");
        }

        String sanitized = rawResponse.trim();
        if (sanitized.startsWith("```")) {
            sanitized = sanitized.replaceFirst("^```(?:json)?\\s*", "");
            sanitized = sanitized.replaceFirst("\\s*```$", "");
        }
        return sanitized.trim();
    }

    private AiSemanticProductMatch toMatch(RawMatch rawMatch) {
        if (rawMatch.productId() == null) {
            throw new AiInvalidModelResponseException("Semantic product match was missing productId.");
        }
        double confidence = rawMatch.confidence() == null ? 0.0d : rawMatch.confidence();
        return new AiSemanticProductMatch(rawMatch.productId(), confidence, rawMatch.reason());
    }

    private void ensureUniqueProductIds(List<AiSemanticProductMatch> matches) {
        Set<Long> seen = new HashSet<>();
        for (AiSemanticProductMatch match : matches) {
            if (!seen.add(match.productId())) {
                throw new AiInvalidModelResponseException("Semantic product selection returned duplicate product IDs.");
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RawSelection(List<RawMatch> matches) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RawMatch(Long productId, Double confidence, String reason) {
    }
}
