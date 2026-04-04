package com.anbu.aipos.ai.application;

public class AiInvalidModelResponseException extends AiPlanningException {
    public AiInvalidModelResponseException(String message) {
        super(message);
    }

    public AiInvalidModelResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
