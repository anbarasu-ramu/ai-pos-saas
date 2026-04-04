package com.anbu.aipos.ai.application;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiToolRegistry {

    private static final List<AiToolDescriptor> DESCRIPTORS = List.of(
            new AiToolDescriptor(AiTool.SEARCH_PRODUCTS, true, "Search products by free-text query.", "{\"query\":\"cappuccino\",\"activeOnly\":true,\"limit\":10}"),
            new AiToolDescriptor(AiTool.GET_PRODUCT_BY_ID, true, "Get one product by numeric productId.", "{\"productId\":1}"),
            new AiToolDescriptor(AiTool.GET_LOW_STOCK_PRODUCTS, true, "List products at or below a stock threshold.", "{\"threshold\":5}"),
            new AiToolDescriptor(AiTool.LIST_PRODUCTS, true, "List tenant products.", "{\"activeOnly\":true}"),
            new AiToolDescriptor(AiTool.GET_ORDERS, true, "List recent orders with paging.", "{\"page\":0,\"size\":20}"),
            new AiToolDescriptor(AiTool.GET_ORDER_DETAIL, true, "Get order detail by numeric orderId.", "{\"orderId\":1}"),
            new AiToolDescriptor(AiTool.GET_DAILY_ORDER_SUMMARY, true, "Get daily order totals for a date and zone.", "{\"date\":\"2026-03-28\",\"zone\":\"Asia/Kolkata\"}"),
            new AiToolDescriptor(AiTool.CREATE_CHECKOUT_ORDER, false, "Create a checkout order. Use only after explicit confirmation from the user.", "{\"items\":[{\"productQuery\":\"cappuccino\",\"quantity\":2}],\"paymentType\":\"CASH\",\"amountPaid\":500.00}"),
            new AiToolDescriptor(AiTool.GET_CURRENT_USER_CONTEXT, true, "Return current authenticated user context.", "{}"),
            new AiToolDescriptor(AiTool.GET_CURRENT_TENANT_CONTEXT, true, "Return current tenant context.", "{}")
    );

    public List<AiToolDescriptor> list() {
        return DESCRIPTORS;
    }

    public boolean isRegistered(AiTool tool) {
        return Arrays.stream(AiTool.values()).anyMatch(candidate -> candidate == tool);
    }

    public String formatForPrompt() {
        return DESCRIPTORS.stream()
                .map(descriptor -> "- %s | readOnly=%s | %s | args=%s".formatted(
                        descriptor.name().name(),
                        descriptor.readOnly(),
                        descriptor.description(),
                        descriptor.arguments()))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }
}
