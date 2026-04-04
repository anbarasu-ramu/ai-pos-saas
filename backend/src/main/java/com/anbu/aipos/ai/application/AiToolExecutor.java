package com.anbu.aipos.ai.application;

import com.anbu.aipos.adapters.in.web.dto.order.DailyOrderSummaryResponse;
import com.anbu.aipos.adapters.in.web.dto.order.OrderDetailResponse;
import com.anbu.aipos.adapters.in.web.dto.order.checkout.CheckoutApiResponse;
import com.anbu.aipos.adapters.in.web.dto.product.ProductSearchResponse;
import com.anbu.aipos.application.order.OrderQueryService;
import com.anbu.aipos.application.product.ProductService;
import com.anbu.aipos.application.tenant.TenantQueryService;
import com.anbu.aipos.core.port.in.order.CheckoutUseCase;
import com.anbu.aipos.core.port.in.product.ProductView;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class AiToolExecutor {

    private final ProductService productService;
    private final OrderQueryService orderQueryService;
    private final CheckoutUseCase checkoutUseCase;
    private final TenantQueryService tenantQueryService;
    private final AiProductSelectionAgent productSelectionAgent;

    public AiToolExecutor(
            ProductService productService,
            OrderQueryService orderQueryService,
            CheckoutUseCase checkoutUseCase,
            TenantQueryService tenantQueryService,
            AiProductSelectionAgent productSelectionAgent
    ) {
        this.productService = productService;
        this.orderQueryService = orderQueryService;
        this.checkoutUseCase = checkoutUseCase;
        this.tenantQueryService = tenantQueryService;
        this.productSelectionAgent = productSelectionAgent;
    }

    public AiToolExecutionResult execute(AiToolCall toolCall, AiExecutionContext context) {
        return switch (toolCall.tool()) {
            case SEARCH_PRODUCTS -> searchProducts(toolCall.arguments(), context);
            case GET_PRODUCT_BY_ID -> getProductById(toolCall.arguments(), context);
            case GET_LOW_STOCK_PRODUCTS -> getLowStockProducts(toolCall.arguments(), context);
            case LIST_PRODUCTS -> listProducts(toolCall.arguments(), context);
            case GET_ORDERS -> getOrders(toolCall.arguments(), context);
            case GET_ORDER_DETAIL -> getOrderDetail(toolCall.arguments(), context);
            case GET_DAILY_ORDER_SUMMARY -> getDailySummary(toolCall.arguments(), context);
            case CREATE_CHECKOUT_ORDER -> createCheckoutOrder(toolCall.arguments(), context);
            case GET_CURRENT_USER_CONTEXT -> currentUser(context);
            case GET_CURRENT_TENANT_CONTEXT -> currentTenant(context);
        };
    }

    private AiToolExecutionResult searchProducts(Map<String, Object> arguments, AiExecutionContext context) {
        String query = stringArg(arguments, "query", stringArg(arguments, "q", ""));
        boolean activeOnly = booleanArg(arguments, "activeOnly", true);
        int limit = intArg(arguments, "limit", 10);
        List<ProductView> catalog = productService.getAll(context.tenantId());
        AiProductSelectionResult selection = productSelectionAgent.selectProducts(query, catalog, activeOnly, limit);
        Map<Long, ProductView> productsById = catalog.stream()
                .filter(product -> product.id() != null)
                .collect(java.util.stream.Collectors.toMap(ProductView::id, product -> product, (left, _right) -> left, LinkedHashMap::new));

        List<Map<String, Object>> matches = selection.matches().stream()
                .map(match -> {
                    ProductView product = productsById.get(match.productId());
                    if (product == null) {
                        return null;
                    }
                    return Map.of(
                            "productId", match.productId(),
                            "confidence", match.confidence(),
                            "reason", match.reason() == null ? "" : match.reason(),
                            "matchType", match.matchType(),
                            "product", product);
                })
                .filter(Objects::nonNull)
                .toList();

        List<ProductView> items = matches.stream()
                .map(match -> (ProductView) match.get("product"))
                .toList();

        if (matches.isEmpty()) {
            return new AiToolExecutionResult(
                    productSearchPayload(query, activeOnly, limit, items, matches, "NO_MATCHES", false),
                    "I couldn’t find a product matching \"%s\".".formatted(query));
        }

        if (isAmbiguousProductSearch(selection, matches)) {
            List<Map<String, Object>> options = matches.stream()
                    .limit(3)
                    .map(match -> Map.of(
                            "productId", match.get("productId"),
                            "confidence", match.get("confidence"),
                            "matchType", match.get("matchType"),
                            "reason", match.get("reason"),
                            "product", match.get("product")))
                    .toList();

            return new AiToolExecutionResult(
                    clarificationPayload(
                            AiTool.SEARCH_PRODUCTS.name(),
                            "AMBIGUOUS_PRODUCT_QUERY",
                            query,
                            options),
                    "I found a few close matches for \"%s\". Which product did you mean?".formatted(query),
                    true);
        }

        return new AiToolExecutionResult(
                productSearchPayload(query, activeOnly, limit, items, matches, "MATCHED", false),
                "Found %d matching product%s.".formatted(items.size(), items.size() == 1 ? "" : "s"));
    }

    private AiToolExecutionResult getProductById(Map<String, Object> arguments, AiExecutionContext context) {
        long productId = longArg(arguments, "productId");
        ProductView product = productService.getById(productId, context.tenantId());
        return new AiToolExecutionResult(product, "Fetched product %s.".formatted(product.name()));
    }

    private AiToolExecutionResult getLowStockProducts(Map<String, Object> arguments, AiExecutionContext context) {
        int threshold = Math.max(0, intArg(arguments, "threshold", 5));
        List<ProductView> items = productService.getLowStockProducts(context.tenantId(), threshold);
        return new AiToolExecutionResult(
                Map.of(
                        "type", "low_stock",
                        "threshold", threshold,
                        "count", items.size(),
                        "items", items),
                "Found %d low-stock product%s.".formatted(items.size(), items.size() == 1 ? "" : "s"));
    }

    private AiToolExecutionResult listProducts(Map<String, Object> arguments, AiExecutionContext context) {
        boolean activeOnly = booleanArg(arguments, "activeOnly", false);
        List<ProductView> items = productService.getAll(context.tenantId()).stream()
                .filter(product -> !activeOnly || Boolean.TRUE.equals(product.active()))
                .toList();
        return new AiToolExecutionResult(
                Map.of(
                        "type", "product_list",
                        "activeOnly", activeOnly,
                        "count", items.size(),
                        "items", items),
                "Listed %d product%s.".formatted(items.size(), items.size() == 1 ? "" : "s"));
    }

    private AiToolExecutionResult getOrders(Map<String, Object> arguments, AiExecutionContext context) {
        int page = intArg(arguments, "page", 0);
        int size = intArg(arguments, "size", 20);
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var orderPage = orderQueryService.getOrders(context.tenantId(), pageable);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "order_list");
        result.put("page", orderPage.getNumber());
        result.put("size", orderPage.getSize());
        result.put("totalElements", orderPage.getTotalElements());
        result.put("totalPages", orderPage.getTotalPages());
        result.put("items", orderPage.getContent());
        return new AiToolExecutionResult(
                result,
                "Loaded %d order%s from page %d.".formatted(
                        orderPage.getNumberOfElements(),
                        orderPage.getNumberOfElements() == 1 ? "" : "s",
                        orderPage.getNumber()));
    }

    private AiToolExecutionResult getOrderDetail(Map<String, Object> arguments, AiExecutionContext context) {
        long orderId = longArg(arguments, "orderId");
        OrderDetailResponse order = orderQueryService.getOrder(context.tenantId(), orderId);
        return new AiToolExecutionResult(
                Map.of(
                        "type", "order_detail",
                        "order", order),
                "Fetched order #%d.".formatted(order.id()));
    }

    private AiToolExecutionResult getDailySummary(Map<String, Object> arguments, AiExecutionContext context) {
        ZoneId zoneId = resolveZone(arguments);
        LocalDate date = LocalDate.parse(stringArg(arguments, "date", LocalDate.now(zoneId).toString()));
        DailyOrderSummaryResponse summary = orderQueryService.getDailySummary(context.tenantId(), date, zoneId);
        return new AiToolExecutionResult(
                Map.of(
                        "type", "daily_summary",
                        "zone", zoneId.getId(),
                        "businessDate", summary.businessDate(),
                        "summary", summary),
                "Daily summary for %s has %d total orders and revenue %s.".formatted(
                        summary.businessDate(),
                        summary.totalOrders(),
                        summary.totalRevenue()));
    }

    private AiToolExecutionResult createCheckoutOrder(Map<String, Object> arguments, AiExecutionContext context) {
        List<CheckoutUseCase.Item> items = resolveCheckoutItems(arguments, context.tenantId());
        String paymentType = stringArg(arguments, "paymentType", "").toUpperCase(Locale.ROOT);
        if (paymentType.isBlank()) {
            throw new IllegalArgumentException("paymentType is required for checkout.");
        }

        String amountPaidRaw = stringArg(arguments, "amountPaid", null);
        if (amountPaidRaw == null || amountPaidRaw.isBlank()) {
            Object rawAmount = arguments.get("amountPaid");
            if (rawAmount != null) {
                amountPaidRaw = rawAmount.toString();
            }
        }
        if (amountPaidRaw == null || amountPaidRaw.isBlank()) {
            throw new IllegalArgumentException("amountPaid is required for checkout.");
        }

        var response = checkoutUseCase.checkout(new CheckoutUseCase.CheckoutCommand(
                items,
                paymentType,
                new BigDecimal(amountPaidRaw),
                context.tenantId(),
                context.userIdAsUuid(),
                context.username()
        ));

        CheckoutApiResponse apiResponse = new CheckoutApiResponse(
                response.orderId(),
                response.totalAmount(),
                response.change(),
                response.status(),
                List.of()
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "checkout_result");
        result.put("orderId", response.orderId());
        result.put("totalAmount", response.totalAmount());
        result.put("change", response.change());
        result.put("status", response.status());
        result.put("items", response.items().stream()
                .map(item -> Map.of(
                        "productId", item.productId(),
                        "name", item.name(),
                        "quantity", item.quantity(),
                        "price", item.price()))
                .toList());

        return new AiToolExecutionResult(
                result,
                "Created order #%d with total %s.".formatted(apiResponse.orderId(), apiResponse.totalAmount()));
    }

    private AiToolExecutionResult currentUser(AiExecutionContext context) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "user_context");
        result.put("user", Map.of(
                "subject", context.subject(),
                "username", context.username(),
                "email", context.email(),
                "roles", context.roles(),
                "tenantId", context.tenantId(),
                "tenantName", context.tenantName() == null ? "" : context.tenantName()));
        return new AiToolExecutionResult(result, "Fetched current user context.");
    }

    private AiToolExecutionResult currentTenant(AiExecutionContext context) {
        String tenantName = context.tenantName();
        if (tenantName == null || tenantName.isBlank()) {
            tenantName = tenantQueryService.findNameById(java.util.UUID.fromString(context.tenantId()));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "tenant_context");
        result.put("tenant", Map.of(
                "tenantId", context.tenantId(),
                "tenantName", tenantName));
        return new AiToolExecutionResult(result, "Fetched current tenant context.");
    }

    private List<CheckoutUseCase.Item> resolveCheckoutItems(Map<String, Object> arguments, String tenantId) {
        Object rawItems = arguments.get("items");
        if (!(rawItems instanceof List<?> rawItemList) || rawItemList.isEmpty()) {
            throw new IllegalArgumentException("items are required for checkout.");
        }

        List<CheckoutUseCase.Item> items = new ArrayList<>();
        for (Object rawItem : rawItemList) {
            if (!(rawItem instanceof Map<?, ?> itemMap)) {
                throw new IllegalArgumentException("Each checkout item must be an object.");
            }

            int quantity = intArg(castMap(itemMap), "quantity", 1);
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be greater than zero.");
            }

            Long productId = nullableLongArg(castMap(itemMap), "productId");
            if (productId == null) {
                String productQuery = stringArg(castMap(itemMap), "productQuery", stringArg(castMap(itemMap), "query", ""));
                if (productQuery.isBlank()) {
                    throw new IllegalArgumentException("Each checkout item needs productId or productQuery.");
                }
                productId = resolveProductIdByQuery(tenantId, productQuery);
            }

            items.add(new CheckoutUseCase.Item(productId, quantity));
        }

        return items;
    }

    private Long resolveProductIdByQuery(String tenantId, String query) {
        List<ProductView> catalog = productService.getAll(tenantId);
        AiProductSelectionResult selection = productSelectionAgent.selectProducts(query, catalog, true, 3);

        if (selection.matches().isEmpty()) {
            throw new IllegalArgumentException("No semantic product match found for query: " + query);
        }

        AiSemanticProductMatch bestMatch = selection.matches().getFirst();
        if (bestMatch.confidence() < 0.55d) {
            throw new IllegalArgumentException("No confident semantic product match found for query: " + query);
        }
        if (selection.matches().size() > 1) {
            AiSemanticProductMatch secondMatch = selection.matches().get(1);
            if (Math.abs(bestMatch.confidence() - secondMatch.confidence()) < 0.08d) {
                throw new IllegalArgumentException("Product query is ambiguous: " + query);
            }
        }

        return bestMatch.productId();
    }

    private ZoneId resolveZone(Map<String, Object> arguments) {
        String requestedZone = stringArg(arguments, "zone", "").trim();
        if (!requestedZone.isBlank()) {
            return ZoneId.of(requestedZone);
        }
        return ZoneId.of("UTC");
    }

    private boolean isAmbiguousProductSearch(
            AiProductSelectionResult selection,
            List<Map<String, Object>> matches
    ) {
        if (selection.matches().size() < 2 || matches.size() < 2) {
            return false;
        }

        AiSemanticProductMatch first = selection.matches().get(0);
        AiSemanticProductMatch second = selection.matches().get(1);
        if ("EXACT_NAME".equals(first.matchType()) && first.confidence() >= 0.99d) {
            return false;
        }

        return first.confidence() < 0.93d || Math.abs(first.confidence() - second.confidence()) < 0.08d;
    }

    private Map<String, Object> productSearchPayload(
            String query,
            boolean activeOnly,
            int limit,
            List<ProductView> items,
            List<Map<String, Object>> matches,
            String status,
            boolean clarification
    ) {
        ProductSearchResponse response = new ProductSearchResponse(query, limit, items.size(), items);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "product_search");
        result.put("status", status);
        result.put("clarification", clarification);
        result.put("query", query);
        result.put("activeOnly", activeOnly);
        result.put("limit", limit);
        result.put("count", items.size());
        result.put("items", items);
        result.put("matches", matches);
        result.put("search", response);
        return result;
    }

    private Map<String, Object> clarificationPayload(
            String targetIntent,
            String reason,
            String query,
            List<Map<String, Object>> options
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "clarification");
        result.put("targetIntent", targetIntent);
        result.put("reason", reason);
        result.put("query", query);
        result.put("options", options);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Map<?, ?> rawMap) {
        return (Map<String, Object>) rawMap;
    }

    private String stringArg(Map<String, Object> arguments, String key, String defaultValue) {
        Object value = arguments.get(key);
        return value == null ? defaultValue : Objects.toString(value, defaultValue);
    }

    private boolean booleanArg(Map<String, Object> arguments, String key, boolean defaultValue) {
        Object value = arguments.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value instanceof Boolean bool ? bool : Boolean.parseBoolean(value.toString());
    }

    private int intArg(Map<String, Object> arguments, String key, int defaultValue) {
        Object value = arguments.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value instanceof Number number ? number.intValue() : Integer.parseInt(value.toString());
    }

    private long longArg(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value == null) {
            throw new IllegalArgumentException(key + " is required.");
        }
        return value instanceof Number number ? number.longValue() : Long.parseLong(value.toString());
    }

    private Long nullableLongArg(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value == null) {
            return null;
        }
        return value instanceof Number number ? number.longValue() : Long.parseLong(value.toString());
    }
}
