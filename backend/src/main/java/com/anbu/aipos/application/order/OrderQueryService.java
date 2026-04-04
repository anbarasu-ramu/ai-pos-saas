package com.anbu.aipos.application.order;

import com.anbu.aipos.adapters.in.web.dto.order.DailyOrderSummaryResponse;
import com.anbu.aipos.adapters.in.web.dto.order.OrderDetailItemResponse;
import com.anbu.aipos.adapters.in.web.dto.order.OrderDetailResponse;
import com.anbu.aipos.adapters.in.web.dto.order.OrderSummaryResponse;
import com.anbu.aipos.adapters.out.persistence.order.OrderItemJpaRepository;
import com.anbu.aipos.adapters.out.persistence.order.OrderJpaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderQueryService {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;

    public OrderQueryService(
            OrderJpaRepository orderRepository,
            OrderItemJpaRepository orderItemRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Page<OrderSummaryResponse> getOrders(String tenantId, Pageable pageable) {

        return orderRepository.findByTenantId(tenantId, pageable)
                .map(order -> new OrderSummaryResponse(
                        order.getId(),
                        order.getTotalAmount(),
                        order.getStatus(),
                        order.getCreatedAt(),
                        order.getCreatedByUsername()
                ));
    }

    public OrderDetailResponse getOrder(String tenantId, Long orderId) {

        var order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        var items = orderItemRepository.findAllByOrderIdAndTenantId(orderId, tenantId)
                .stream()
                .map(item -> new OrderDetailItemResponse(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        return new OrderDetailResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getCreatedByUsername(),
                order.getCreatedByUserId(),
                items
        );
    }

    public DailyOrderSummaryResponse getDailySummary(String tenantId, LocalDate businessDate, ZoneId zoneId) {

        var rangeStart = businessDate.atStartOfDay(zoneId).toInstant();
        var rangeEnd = businessDate.plusDays(1).atStartOfDay(zoneId).toInstant();
        var totalOrders = orderRepository.countByTenantIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                tenantId,
                rangeStart,
                rangeEnd
        );
        var completedOrders = orderRepository.countByTenantIdAndStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                tenantId,
                "COMPLETED",
                rangeStart,
                rangeEnd
        );
        var totalRevenue = orderRepository.sumTotalAmountForPeriod(tenantId, rangeStart, rangeEnd);
        var averageOrderValue = totalOrders == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

        return new DailyOrderSummaryResponse(
                businessDate,
                rangeStart,
                rangeEnd,
                totalOrders,
                completedOrders,
                totalRevenue,
                averageOrderValue
        );
    }
}
