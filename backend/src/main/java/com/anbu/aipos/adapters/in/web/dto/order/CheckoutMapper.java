package com.anbu.aipos.adapters.in.web.dto.order;


import com.anbu.aipos.core.port.in.order.CheckoutUseCase;

import java.util.stream.Collectors;

public class CheckoutMapper {

    public static CheckoutUseCase.CheckoutCommand toCommand(CheckoutRequest request,String tenantId) {
        return new CheckoutUseCase.CheckoutCommand(
                request.items().stream()
                        .map(i -> new CheckoutUseCase.Item(i.productId(), i.quantity()))
                        .collect(Collectors.toList()),
                request.paymentType(),
                request.amountPaid(),tenantId
        );
    }

    public static CheckoutApiResponse toResponse(CheckoutUseCase.CheckoutResponse response) {
        return new CheckoutApiResponse(
                response.orderId(),
                response.totalAmount(),
                response.change(),
                response.status(),
                response.items().stream().map(item ->
                        new CheckoutItem(item.name(), item.quantity(), item.price()))
                        .collect(Collectors.toList())
        );
    }
}