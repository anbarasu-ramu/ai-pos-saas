package com.anbu.aipos.adapters.in.web.order;

import com.anbu.aipos.adapters.in.web.dto.order.DailyOrderSummaryResponse;
import com.anbu.aipos.adapters.in.web.dto.order.OrderDetailResponse;
import com.anbu.aipos.adapters.in.web.dto.order.OrderSummaryResponse;
import com.anbu.aipos.application.order.OrderQueryService;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderQueryService orderQueryService;

    public OrderController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @GetMapping
    public Page<OrderSummaryResponse> getOrders(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        var tenantId = jwt.getClaimAsString("tenant_id");
        return orderQueryService.getOrders(tenantId, pageable);
    }

    @GetMapping("/{id}")
    public OrderDetailResponse getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {

        var tenantId = jwt.getClaimAsString("tenant_id");
        return orderQueryService.getOrder(tenantId, id);
    }

    @GetMapping("/summary")
    public DailyOrderSummaryResponse getSummary(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "UTC") String zone
    ) {

        var tenantId = jwt.getClaimAsString("tenant_id");
        var zoneId = ZoneId.of(zone);
        var businessDate = date != null ? date : LocalDate.now(zoneId);
        return orderQueryService.getDailySummary(tenantId, businessDate, zoneId);
    }
}
