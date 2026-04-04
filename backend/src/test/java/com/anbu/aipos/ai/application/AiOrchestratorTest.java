package com.anbu.aipos.ai.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anbu.aipos.application.tenant.TenantQueryService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;

class AiOrchestratorTest {

    private final OllamaClient ollamaClient = mock(OllamaClient.class);
    private final LocalAiPlanner localAiPlanner = new LocalAiPlanner();
    private final AiToolRegistry toolRegistry = new AiToolRegistry();
    private final AiToolExecutionPolicy executionPolicy = new AiToolExecutionPolicy();
    private final AiToolExecutor toolExecutor = mock(AiToolExecutor.class);
    private final TenantQueryService tenantQueryService = mock(TenantQueryService.class);
    private final AiOrchestrator orchestrator = new AiOrchestrator(
            ollamaClient,
            localAiPlanner,
            toolRegistry,
            executionPolicy,
            toolExecutor,
            tenantQueryService
    );

    @Test
    void executesLowStockToolWhenOllamaIsUnavailable() {
        Jwt jwt = jwt();
        when(tenantQueryService.findNameById(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")))
                .thenReturn("Demo Store");
        when(ollamaClient.plan(any(), any(), any()))
                .thenThrow(new AiModelUnavailableException("down", new RuntimeException("offline")));
        when(toolExecutor.execute(any(), any()))
                .thenReturn(new AiToolExecutionResult(Map.of("type", "low_stock", "count", 2), "Found 2 low-stock products."));

        AiChatResponse response = orchestrator.handle("show low stock items", jwt);

        ArgumentCaptor<AiToolCall> toolCaptor = ArgumentCaptor.forClass(AiToolCall.class);
        ArgumentCaptor<AiExecutionContext> contextCaptor = ArgumentCaptor.forClass(AiExecutionContext.class);
        verify(toolExecutor).execute(toolCaptor.capture(), contextCaptor.capture());
        assertEquals(AiTool.GET_LOW_STOCK_PRODUCTS, toolCaptor.getValue().tool());
        assertEquals("11111111-1111-1111-1111-111111111111", contextCaptor.getValue().tenantId());
        assertFalse(response.requiresConfirmation());
        assertEquals("GET_LOW_STOCK_PRODUCTS", response.intent());
        assertEquals("Found 2 low-stock products.", response.assistantMessage());
    }

    @Test
    void prefersDeterministicPlannerForExplicitLowStockPrompt() {
        Jwt jwt = jwt();
        when(tenantQueryService.findNameById(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")))
                .thenReturn("Demo Store");
        when(toolExecutor.execute(any(), any()))
                .thenReturn(new AiToolExecutionResult(Map.of("type", "low_stock", "threshold", 20, "count", 1), "Found 1 low-stock product."));

        AiChatResponse response = orchestrator.handle("get me low stock products under 20", jwt);

        verify(ollamaClient, never()).plan(any(), any(), any());
        ArgumentCaptor<AiToolCall> toolCaptor = ArgumentCaptor.forClass(AiToolCall.class);
        verify(toolExecutor).execute(toolCaptor.capture(), any());
        assertEquals(AiTool.GET_LOW_STOCK_PRODUCTS, toolCaptor.getValue().tool());
        assertEquals(20, toolCaptor.getValue().arguments().get("threshold"));
        assertEquals("GET_LOW_STOCK_PRODUCTS", response.intent());
    }

    @Test
    void returnsClarificationPayloadWithoutTreatingItAsConfirmation() {
        Jwt jwt = jwt();
        when(tenantQueryService.findNameById(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")))
                .thenReturn("Demo Store");
        when(ollamaClient.plan(any(), any(), any()))
                .thenThrow(new AiModelUnavailableException("down", new RuntimeException("offline")));
        when(toolExecutor.execute(any(), any()))
                .thenReturn(new AiToolExecutionResult(
                        Map.of("type", "clarification", "reason", "AMBIGUOUS_PRODUCT_QUERY"),
                        "I found a few close matches for \"coffee\". Which product did you mean?",
                        true));

        AiChatResponse response = orchestrator.handle("find coffee", jwt);

        assertFalse(response.requiresConfirmation());
        assertEquals("SEARCH_PRODUCTS", response.intent());
        assertEquals("clarification", ((Map<?, ?>) response.result()).get("type"));
    }

    @Test
    void doesNotExecuteToolWhenModelResponseIsInvalid() {
        Jwt jwt = jwt();
        when(tenantQueryService.findNameById(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")))
                .thenReturn("Demo Store");
        when(ollamaClient.plan(any(), any(), any()))
                .thenThrow(new AiInvalidModelResponseException("bad json"));

        AiChatResponse response = orchestrator.handle("how is the store doing overall?", jwt);

        verify(toolExecutor, never()).execute(any(), any());
        assertEquals("TEXT_RESPONSE", response.intent());
        assertTrue(response.toolInvocations().isEmpty());
    }

    @Test
    void requiresConfirmationBeforeCheckoutExecution() {
        Jwt jwt = jwt();
        when(tenantQueryService.findNameById(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111")))
                .thenReturn("Demo Store");
        when(ollamaClient.plan(any(), any(), any()))
                .thenThrow(new AiModelUnavailableException("down", new RuntimeException("offline")));

        AiChatResponse response = orchestrator.handle("create order for 2 cappuccinos", jwt);

        verify(toolExecutor, never()).execute(any(), any());
        assertTrue(response.requiresConfirmation());
        assertEquals(AiTool.CREATE_CHECKOUT_ORDER.name(), response.intent());
        assertEquals("CONFIRMATION_REQUIRED", response.toolInvocations().getFirst().status());
    }

    private Jwt jwt() {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("11111111-1111-1111-1111-111111111111")
                .claim("tenant_id", "11111111-1111-1111-1111-111111111111")
                .claim("preferred_username", "cashier")
                .claim("email", "cashier@example.com")
                .claim("realm_access", Map.of("roles", List.of("CASHIER")))
                .build();
    }
}
