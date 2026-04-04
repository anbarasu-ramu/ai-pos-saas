package com.anbu.aipos.ai.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class AiModelDecisionParserTest {

    private final AiModelDecisionParser parser = new AiModelDecisionParser(new ObjectMapper());

    @Test
    void parsesFencedJsonResponse() {
        String response = """
                ```json
                {
                  "assistantMessage": "Found low stock items.",
                  "intent": "GET_LOW_STOCK_PRODUCTS",
                  "toolCalls": [
                    {
                      "tool": "GET_LOW_STOCK_PRODUCTS",
                      "arguments": { "threshold": 5 }
                    }
                  ],
                  "requiresConfirmation": false
                }
                ```
                """;

        AiModelDecision decision = parser.parse(response);

        assertEquals("GET_LOW_STOCK_PRODUCTS", decision.intent());
        assertEquals(1, decision.toolCalls().size());
        assertEquals(AiTool.GET_LOW_STOCK_PRODUCTS, decision.toolCalls().getFirst().tool());
        assertEquals(5, ((Number) decision.toolCalls().getFirst().arguments().get("threshold")).intValue());
        assertFalse(decision.requiresConfirmation());
    }

    @Test
    void rejectsInvalidResponse() {
        assertThrows(AiInvalidModelResponseException.class, () -> parser.parse("not-json"));
    }
}
