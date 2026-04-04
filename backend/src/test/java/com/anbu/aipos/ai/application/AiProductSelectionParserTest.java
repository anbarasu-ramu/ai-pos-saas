package com.anbu.aipos.ai.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class AiProductSelectionParserTest {

    private final AiProductSelectionParser parser = new AiProductSelectionParser(new ObjectMapper());

    @Test
    void parsesValidStructuredSelection() {
        AiProductSelectionResult result = parser.parse("""
                {
                  "matches": [
                    { "productId": 23, "confidence": 0.92, "reason": "Closest dessert match" }
                  ]
                }
                """);

        assertEquals(1, result.matches().size());
        assertEquals(23L, result.matches().getFirst().productId());
        assertEquals(0.92d, result.matches().getFirst().confidence());
    }

    @Test
    void rejectsDuplicateProductIds() {
        assertThrows(AiInvalidModelResponseException.class, () -> parser.parse("""
                {
                  "matches": [
                    { "productId": 23, "confidence": 0.92, "reason": "one" },
                    { "productId": 23, "confidence": 0.83, "reason": "two" }
                  ]
                }
                """));
    }
}
