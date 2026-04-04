package com.anbu.aipos.ai.application;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LocalAiPlanner {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+)");
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("order\\s+#?(\\d+)");
    private static final Pattern ISO_DATE_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");

    public AiModelDecision plan(String userMessage) {
        String normalized = userMessage == null ? "" : userMessage.trim();
        String lower = normalized.toLowerCase();

        // ===============================
        // 1. CHECKOUT (MOST SPECIFIC)
        // ===============================
        if (lower.contains("create order") || lower.contains("checkout")) {
            return planCheckout(normalized, lower);
        }

        // ===============================
        // 2. ORDER DETAIL
        // ===============================
        Matcher orderIdMatcher = ORDER_ID_PATTERN.matcher(lower);
        if (orderIdMatcher.find()) {
            long orderId = Long.parseLong(orderIdMatcher.group(1));
            return decision(
                    "I can fetch that order.",
                    AiTool.GET_ORDER_DETAIL,
                    Map.of("orderId", orderId),
                    false
            );
        }

        // ===============================
        // 3. ORDER HISTORY
        // ===============================
        if (lower.contains("order history") || lower.equals("orders") || lower.contains("recent orders")) {
            return decision(
                    "I can list recent orders.",
                    AiTool.GET_ORDERS,
                    Map.of("page", 0, "size", 20),
                    false
            );
        }

        // ===============================
        // 4. LOW STOCK
        // ===============================
        if (lower.contains("low stock")) {
            int threshold = extractFirstNumber(lower, 5);
            return decision(
                    "I can check low-stock items for you.",
                    AiTool.GET_LOW_STOCK_PRODUCTS,
                    Map.of("threshold", threshold),
                    false
            );
        }

        // ===============================
        // 5. PRODUCT SEARCH
        // ===============================
        if (lower.contains("find ") || lower.contains("search ")) {
            String query = extractQuery(normalized);

            if (query.isBlank()) {
                return fallback("Which product should I search for? Try something like \"find cappuccino\".");
            }

            return decision(
                    "I can search matching products.",
                    AiTool.SEARCH_PRODUCTS,
                    Map.of("query", query, "activeOnly", true, "limit", 10),
                    false
            );
        }

        // ===============================
        // 6. LIST PRODUCTS
        // ===============================
        if (lower.contains("list products") || lower.equals("products") || lower.contains("show products")) {
            return decision(
                    "I can list the current products.",
                    AiTool.LIST_PRODUCTS,
                    Map.of("activeOnly", true),
                    false
            );
        }

        // ===============================
        // 7. DAILY SUMMARY
        // ===============================
        if (lower.contains("sales summary") || lower.contains("order summary") ||
                (lower.contains("today") && lower.contains("sales"))) {
            String requestedDate = extractRequestedDate(lower);
            if (requestedDate == null && (lower.contains("sales summary") || lower.contains("order summary"))) {
                return fallback("Which business date do you want for the sales summary? Try \"today's sales\" or \"sales summary for 2026-04-04\".");
            }

            return decision(
                    "I can fetch the daily sales summary.",
                    AiTool.GET_DAILY_ORDER_SUMMARY,
                    Map.of("date", requestedDate == null ? LocalDate.now().toString() : requestedDate),
                    false
            );
        }

        // ===============================
        // 8. USER CONTEXT
        // ===============================
        if (lower.contains("current user") || lower.contains("who am i") || lower.contains("my context")) {
            return decision(
                    "I can show your current user context.",
                    AiTool.GET_CURRENT_USER_CONTEXT,
                    Map.of(),
                    false
            );
        }

        // ===============================
        // 9. TENANT CONTEXT
        // ===============================
        if (lower.contains("tenant")) {
            return decision(
                    "I can show your current tenant context.",
                    AiTool.GET_CURRENT_TENANT_CONTEXT,
                    Map.of(),
                    false
            );
        }

        // ===============================
        // 10. FALLBACK
        // ===============================
        return fallback("""
            I can help with:
            • "show low stock"
            • "search coffee"
            • "show orders"
            • "today's sales"
            • "checkout 2 coffee"
            """);
    }

    // ===============================
// CHECKOUT PLANNER
// ===============================
    private AiModelDecision planCheckout(String normalized, String lower) {

        boolean confirmed = lower.contains("confirm")
                || lower.contains("go ahead")
                || lower.contains("proceed")
                || lower.contains("place the order")
                || lower.contains("do it");

        // V1: pass raw message instead of strict parsing
        Map<String, Object> args = Map.of(
                "message", normalized
        );

        return new AiModelDecision(
                confirmed
                        ? "Creating the order with the details you confirmed."
                        : "I can prepare that order, but I need your confirmation before creating it.",
                AiTool.CREATE_CHECKOUT_ORDER.name(),
                List.of(new AiToolCall(AiTool.CREATE_CHECKOUT_ORDER, args)),
                !confirmed
        );
    }

    // ===============================
// HELPERS
// ===============================
    private AiModelDecision decision(String message, AiTool tool, Map<String, Object> args, boolean confirm) {
        return new AiModelDecision(
                message,
                tool.name(),
                List.of(new AiToolCall(tool, args)),
                confirm
        );
    }

    private AiModelDecision fallback(String message) {
        return new AiModelDecision(
                message,
                "TEXT_RESPONSE",
                List.of(),
                false
        );
    }

    private int extractFirstNumber(String text, int defaultValue) {
        Matcher matcher = NUMBER_PATTERN.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : defaultValue;
    }

    private String extractQuery(String normalized) {
        String query = normalized.replaceFirst("(?i)^.*?(find|search)\\s+", "").trim();
        return query.isBlank() ? normalized.trim() : query;
    }

    private String extractRequestedDate(String lower) {
        Matcher matcher = ISO_DATE_PATTERN.matcher(lower);
        if (matcher.find()) {
            return matcher.group(1);
        }
        if (lower.contains("yesterday")) {
            return LocalDate.now().minusDays(1).toString();
        }
        if (lower.contains("today")) {
            return LocalDate.now().toString();
        }
        return null;
    }
}
