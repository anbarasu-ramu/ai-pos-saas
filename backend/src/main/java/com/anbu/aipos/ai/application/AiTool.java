package com.anbu.aipos.ai.application;

public enum AiTool {
    SEARCH_PRODUCTS(true),
    GET_PRODUCT_BY_ID(true),
    GET_LOW_STOCK_PRODUCTS(true),
    LIST_PRODUCTS(true),
    GET_ORDERS(true),
    GET_ORDER_DETAIL(true),
    GET_DAILY_ORDER_SUMMARY(true),
    CREATE_CHECKOUT_ORDER(false),
    GET_CURRENT_USER_CONTEXT(true),
    GET_CURRENT_TENANT_CONTEXT(true);

    private final boolean readOnly;

    AiTool(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
