package com.anbu.aipos.application.order;

import com.anbu.aipos.adapters.in.web.dto.order.OrderSummaryResponse;
import com.anbu.aipos.adapters.out.persistence.order.OrderJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrderQueryService {

    private final OrderJpaRepository orderRepository;

    public OrderQueryService(OrderJpaRepository orderRepository) {
        this.orderRepository = orderRepository;
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
}
