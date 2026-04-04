package com.anbu.aipos.application.product;

import com.anbu.aipos.core.domain.product.Product;
import com.anbu.aipos.core.port.in.product.*;
import com.anbu.aipos.core.port.out.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class ProductService implements CreateProductUseCase, GetProductsQuery {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product create(CreateProductCommand command) {
        String normalizedName = command.name() == null ? null : command.name().trim();

        if (repository.existsByTenantIdAndNameIgnoreCase(command.tenantId(), normalizedName)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product already exists with name: " + normalizedName
            );
        }

        Product product = Product.create(
                normalizedName,
                command.category(),
                command.price(),
                command.stockQuantity(),
                command.tenantId()
        );

        return repository.save(product);
    }

    @Override
    public List<ProductView> getAll(String tenantId) {

        return repository.findByTenantId(tenantId)
                .stream()
                .map(this::toView)
                .toList();
    }

    public ProductView update(UpdateProductCommand command) {
        Product product = repository.findByIdAndTenantId(command.id(), command.tenantId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.isActive()) {
            throw new IllegalStateException("Inactive product cannot be modified");
        }

        product.updateName(command.name());
        product.updateCategory(command.category());
        product.updatePrice(command.price());

        int delta = command.stockQuantity() - product.getStockQuantity();
        if (delta > 0) {
            product.increaseStock(delta);
        } else if (delta < 0) {
            product.reduceStock(-delta);
        }

        repository.save(product);

        return toView(product);
    }

    public void deactivate(Long id, String tenantId) {

        Product product = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.deactivate();
        repository.save(product);
    }

    public void activate(Long id, String tenantId) {

        Product product = repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.activate();
        repository.save(product);
    }

    public ProductView getById(Long id, String tenantId) {
        return repository.findByIdAndTenantId(id, tenantId)
                .map(this::toView)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public List<ProductView> search(String tenantId, String query, boolean activeOnly, int limit) {
        var normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            return getAll(tenantId).stream()
                    .filter(product -> !activeOnly || Boolean.TRUE.equals(product.active()))
                    .limit(limit)
                    .toList();
        }

        return repository.search(tenantId, normalizedQuery, activeOnly, limit).stream()
                .map(this::toView)
                .toList();
    }

    public List<ProductView> getLowStockProducts(String tenantId, int threshold) {
        return repository.findLowStockProducts(tenantId, threshold)
                .stream()
                .sorted(Comparator.comparing(Product::getStockQuantity).thenComparing(Product::getName))
                .map(this::toView)
                .toList();
    }

    private ProductView toView(Product product) {
        return new ProductView(
                product.getId() != null ? product.getId().value() : null,
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStockQuantity(),
                product.isActive()
        );
    }
}
