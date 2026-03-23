package com.anbu.aipos.adapters.in.web;

import com.anbu.aipos.adapters.in.web.dto.product.CreateProductRequest;
import com.anbu.aipos.application.product.ProductService;
import com.anbu.aipos.core.port.in.product.CreateProductCommand;
import com.anbu.aipos.core.port.in.product.ProductView;
import com.anbu.aipos.core.port.in.product.UpdateProductCommand;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    ProductController(ProductService productService){
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ProductView create(@RequestBody CreateProductRequest request,
                              @AuthenticationPrincipal Jwt jwt) {

        String tenantId = jwt.getClaim("tenant_id");

        var product = productService.create(
                new CreateProductCommand(
                        request.name(),
                        request.category(),
                        request.price(),
                        request.stockQuantity(),
                        tenantId
                )
        );

        return new ProductView(
                product.getId() != null ? product.getId().value() : null,
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStockQuantity(),
                product.isActive()
        );
    }

    @GetMapping
    public List<ProductView> list(@AuthenticationPrincipal Jwt jwt) {
        String tenantId = jwt.getClaim("tenant_id");
        return productService.getAll(tenantId);
    }

    @PutMapping("/{productId}")
    public ProductView update(@PathVariable Long productId,
                              @RequestBody CreateProductRequest request,
                              @AuthenticationPrincipal Jwt jwt) {

        String tenantId = jwt.getClaim("tenant_id");

        return productService.update(new UpdateProductCommand(
                productId,
                request.name(),
                request.category(),
                request.price(),
                request.stockQuantity(),
                tenantId
        ));
    }

    @DeleteMapping("/{productId}")
    public void deactivate(@PathVariable Long productId,
                           @AuthenticationPrincipal Jwt jwt) {

        String tenantId = jwt.getClaim("tenant_id");
        productService.deactivate(productId, tenantId);
    }

    @PutMapping("/{productId}/activate")
    public void activate(@PathVariable Long productId,
                         @AuthenticationPrincipal Jwt jwt) {

        String tenantId = jwt.getClaim("tenant_id");
        productService.activate(productId, tenantId);
    }
}