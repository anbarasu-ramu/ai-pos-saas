package com.anbu.aipos.ai.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LocalAiPlannerTest {

    private final LocalAiPlanner planner = new LocalAiPlanner();

    @Test
    void plansLowStockWithDefaultThreshold() {
        AiModelDecision decision = planner.plan("show low stock");

        assertEquals(AiTool.GET_LOW_STOCK_PRODUCTS.name(), decision.intent());
        assertEquals(5, decision.toolCalls().getFirst().arguments().get("threshold"));
    }

    @Test
    void asksForDateWhenSalesSummaryIsUnderspecified() {
        AiModelDecision decision = planner.plan("sales summary");

        assertEquals("TEXT_RESPONSE", decision.intent());
        assertTrue(decision.assistantMessage().contains("Which business date"));
    }

    @Test
    void supportsExplicitOrderSummaryDate() {
        AiModelDecision decision = planner.plan("sales summary for 2026-04-04");

        assertEquals(AiTool.GET_DAILY_ORDER_SUMMARY.name(), decision.intent());
        assertEquals("2026-04-04", decision.toolCalls().getFirst().arguments().get("date"));
    }
}
