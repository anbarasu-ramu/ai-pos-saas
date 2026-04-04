package com.anbu.aipos.ai.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anbu.aipos.adapters.in.web.dto.order.DailyOrderSummaryResponse;
import com.anbu.aipos.application.order.OrderQueryService;
import com.anbu.aipos.application.product.ProductService;
import com.anbu.aipos.application.tenant.TenantQueryService;
import com.anbu.aipos.core.port.in.product.ProductView;
import com.anbu.aipos.core.port.in.order.CheckoutUseCase;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiToolExecutorTest {

    private final ProductService productService = mock(ProductService.class);
    private final OrderQueryService orderQueryService = mock(OrderQueryService.class);
    private final CheckoutUseCase checkoutUseCase = mock(CheckoutUseCase.class);
    private final TenantQueryService tenantQueryService = mock(TenantQueryService.class);
    private final AiProductSelectionAgent productSelectionAgent = mock(AiProductSelectionAgent.class);
    private final AiToolExecutor executor = new AiToolExecutor(
            productService,
            orderQueryService,
            checkoutUseCase,
            tenantQueryService,
            productSelectionAgent
    );

    @Test
    void usesUtcDefaultsForDailySummaryWhenArgumentsMissing() {
        AiExecutionContext context = new AiExecutionContext(
                "tenant-1",
                "Demo Store",
                "11111111-1111-1111-1111-111111111111",
                "cashier",
                "cashier@example.com",
                List.of("CASHIER")
        );

        DailyOrderSummaryResponse summary = new DailyOrderSummaryResponse(
                LocalDate.now(ZoneId.of("UTC")),
                LocalDate.now(ZoneId.of("UTC")).atStartOfDay(ZoneId.of("UTC")).toInstant(),
                LocalDate.now(ZoneId.of("UTC")).plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant(),
                3,
                3,
                new BigDecimal("150.00"),
                new BigDecimal("50.00")
        );
        when(orderQueryService.getDailySummary(eq("tenant-1"), eq(LocalDate.now(ZoneId.of("UTC"))), eq(ZoneId.of("UTC"))))
                .thenReturn(summary);

        AiToolExecutionResult result = executor.execute(
                new AiToolCall(AiTool.GET_DAILY_ORDER_SUMMARY, Map.of()),
                context
        );

        verify(orderQueryService).getDailySummary("tenant-1", LocalDate.now(ZoneId.of("UTC")), ZoneId.of("UTC"));
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) result.result();
        assertEquals("daily_summary", payload.get("type"));
        assertEquals("UTC", payload.get("zone"));
        assertEquals(summary, payload.get("summary"));
    }

    @Test
    void returnsClarificationWhenTopProductMatchesAreTooClose() {
        AiExecutionContext context = context();
        List<ProductView> catalog = List.of(
                new ProductView(1L, "Iced Coffee", "Beverages", new BigDecimal("120.00"), 8, true),
                new ProductView(2L, "Coffee Beans", "Retail", new BigDecimal("250.00"), 4, true)
        );

        when(productService.getAll("tenant-1")).thenReturn(catalog);
        when(productSelectionAgent.selectProducts(eq("coffee"), eq(catalog), eq(true), eq(10)))
                .thenReturn(new AiProductSelectionResult(List.of(
                        new AiSemanticProductMatch(1L, 0.82d, "Close beverage match", "TOKEN_OVERLAP"),
                        new AiSemanticProductMatch(2L, 0.79d, "Close retail match", "TOKEN_OVERLAP")
                )));

        AiToolExecutionResult result = executor.execute(
                new AiToolCall(AiTool.SEARCH_PRODUCTS, Map.of("query", "coffee", "activeOnly", true, "limit", 10)),
                context
        );

        assertTrue(result.clarification());
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) result.result();
        assertEquals("clarification", payload.get("type"));
        assertEquals("AMBIGUOUS_PRODUCT_QUERY", payload.get("reason"));
    }

    @Test
    void returnsStablePayloadForLowStockQueries() {
        AiExecutionContext context = context();
        List<ProductView> products = List.of(
                new ProductView(1L, "Arabica Beans", "Coffee", new BigDecimal("350.00"), 2, true)
        );
        when(productService.getLowStockProducts("tenant-1", 3)).thenReturn(products);

        AiToolExecutionResult result = executor.execute(
                new AiToolCall(AiTool.GET_LOW_STOCK_PRODUCTS, Map.of("threshold", 3)),
                context
        );

        assertFalse(result.clarification());
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) result.result();
        assertEquals("low_stock", payload.get("type"));
        assertEquals(1, payload.get("count"));
        assertEquals(products, payload.get("items"));
    }

    private AiExecutionContext context() {
        return new AiExecutionContext(
                "tenant-1",
                "Demo Store",
                "11111111-1111-1111-1111-111111111111",
                "cashier",
                "cashier@example.com",
                List.of("CASHIER")
        );
    }
}
