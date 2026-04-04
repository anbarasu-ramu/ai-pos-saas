package com.anbu.aipos.ai.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.anbu.aipos.core.port.in.product.ProductView;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiProductSelectionAgentTest {

    private final OllamaClient ollamaClient = mock(OllamaClient.class);
    private final AiProductSelectionAgent agent = new AiProductSelectionAgent(ollamaClient);

    @Test
    void returnsValidatedMatchesFromCatalogOnly() {
        when(ollamaClient.selectProducts(anyString(), anyList(), anyInt()))
                .thenReturn(new AiProductSelectionResult(List.of(
                        new AiSemanticProductMatch(23L, 0.92d, "Closest dessert match")
                )));

        List<ProductView> catalog = List.of(
                new ProductView(23L, "Chocolate Lava Cake", "Desserts", BigDecimal.TEN, 8, true)
        );

        AiProductSelectionResult result = agent.selectProducts("choco lava cake", catalog, true, 5);

        assertEquals(1, result.matches().size());
        assertEquals(23L, result.matches().getFirst().productId());
        assertEquals("SEMANTIC", result.matches().getFirst().matchType());
    }

    @Test
    void fallsBackToDeterministicMatchingWhenModelReturnsInventedIds() {
        when(ollamaClient.selectProducts(anyString(), anyList(), anyInt()))
                .thenReturn(new AiProductSelectionResult(List.of(
                        new AiSemanticProductMatch(999L, 0.92d, "Invented")
                )));

        List<ProductView> catalog = List.of(
                new ProductView(23L, "Chocolate Lava Cake", "Desserts", BigDecimal.TEN, 8, true)
        );

        AiProductSelectionResult result = agent.selectProducts("choco lava cake", catalog, true, 5);

        assertEquals(1, result.matches().size());
        assertEquals(23L, result.matches().getFirst().productId());
        assertEquals("TOKEN_OVERLAP", result.matches().getFirst().matchType());
    }
}
