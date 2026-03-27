package com.anbu.aipos.adapters.in.web.order.checkout;

import com.anbu.aipos.adapters.in.web.dto.order.checkout.CheckoutApiResponse;
import com.anbu.aipos.adapters.in.web.dto.order.checkout.CheckoutMapper;
import com.anbu.aipos.adapters.in.web.dto.order.checkout.CheckoutRequest;
import com.anbu.aipos.core.port.in.order.CheckoutUseCase;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {
    private final CheckoutUseCase checkoutUseCase;
    public CheckoutController(CheckoutUseCase checkoutUseCase){
        this.checkoutUseCase = checkoutUseCase;
    }

    @PostMapping
    public CheckoutApiResponse checkout(@RequestBody CheckoutRequest request, @AuthenticationPrincipal Jwt jwt) {
        String tenantId = jwt.getClaim("tenant_id");
        var command = CheckoutMapper.toCommand(request,jwt);
        var result = checkoutUseCase.checkout(command);
        return CheckoutMapper.toResponse(result);
    }
}
