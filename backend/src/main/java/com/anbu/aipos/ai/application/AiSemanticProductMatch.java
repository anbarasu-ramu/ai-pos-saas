package com.anbu.aipos.ai.application;

public record AiSemanticProductMatch(
        Long productId,
        double confidence,
        String reason,
        String matchType
) {
    public AiSemanticProductMatch(Long productId, double confidence, String reason) {
        this(productId, confidence, reason, "SEMANTIC");
    }
}
