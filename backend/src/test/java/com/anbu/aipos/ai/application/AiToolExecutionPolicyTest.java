package com.anbu.aipos.ai.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiToolExecutionPolicyTest {

    private final AiToolExecutionPolicy policy = new AiToolExecutionPolicy();

    @Test
    void requiresConfirmationForMutatingToolWithoutExplicitApproval() {
        AiModelDecision decision = new AiModelDecision(
                "Preparing checkout.",
                AiTool.CREATE_CHECKOUT_ORDER.name(),
                List.of(new AiToolCall(AiTool.CREATE_CHECKOUT_ORDER, Map.of("paymentType", "CASH"))),
                false
        );

        assertTrue(policy.requiresConfirmation(decision, "create order for 2 cappuccinos"));
    }

    @Test
    void allowsMutatingToolWhenMessageExplicitlyConfirms() {
        AiModelDecision decision = new AiModelDecision(
                "Creating checkout.",
                AiTool.CREATE_CHECKOUT_ORDER.name(),
                List.of(new AiToolCall(AiTool.CREATE_CHECKOUT_ORDER, Map.of("paymentType", "CASH"))),
                false
        );

        assertFalse(policy.requiresConfirmation(decision, "confirm create order for 2 cappuccinos"));
    }
}
