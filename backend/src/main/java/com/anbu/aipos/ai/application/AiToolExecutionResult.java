package com.anbu.aipos.ai.application;

public record AiToolExecutionResult(
        Object result,
        String summary,
        boolean clarification
) {
    public AiToolExecutionResult(Object result, String summary) {
        this(result, summary, false);
    }
}
