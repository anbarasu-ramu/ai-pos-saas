package com.anbu.aipos.ai.application;

public class AiPlanningException extends RuntimeException {
    public AiPlanningException(String message) {
        super(message);
    }

    public AiPlanningException(String message, Throwable cause) {
        super(message, cause);
    }
}
