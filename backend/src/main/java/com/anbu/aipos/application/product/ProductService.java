package com.anbu.aipos.application.product;

import com.anbu.aipos.core.domain.product.Product;
import com.anbu.aipos.core.port.in.product.CreateProductCommand;
import com.anbu.aipos.core.port.in.product.CreateProductUseCase;
import com.anbu.aipos.core.port.in.product.GetProductsQuery;
import com.anbu.aipos.core.port.in.product.ProductView;import com.anbu.aipos.core.port.in.product.UpdateProductCommand;import com.anbu.aipos.core.port.out.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService implements CreateProductUseCase, GetProductsQuery {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository){
        this.repository = repository;
    }

    // 🔹 CREATE (Command)
    @Override
    public Product create(CreateProductCommand command) {

        Product product = Product.create(
                command.name(),
                command.category(),
                command.price(),
                command.stockQuantity(),
                command.tenantId()
        );

        return repository.save(product);
    }

    // 🔹 READ (Query)
    @Override
    public List<ProductView> getAll(String tenantId) {

        return repository.findByTenantId(tenantId)
                .stream()
                .map(p -> new ProductView(
                        p.getId() != null ? p.getId().value() : null,
                        p.getName(),
                        p.getCategory(),
                        p.getPrice(),
                        p.getStockQuantity(),
                        p.isActive()
                ))
                .toList();
    }

    // 🔹 DELETE → prefer deactivate in POS
    public ProductView update(UpdateProductCommand command) {
        Product product = repository.findByIdAndTenantId(command.id(), command.tenantId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.isActive()) {
            throw new IllegalStateException("Inactive product cannot be modified");
        }

        product.updateName(command.name());
        product.updatePrice(command.price());

        int delta = command.stockQuantity() - product.getStockQuantity();
        if (delta > 0) {
            product.increaseStock(delta);
        } else if (delta < 0) {
            product.reduceStock(-delta);
        }

        repository.save(product);

        return new ProductView(
                product.getId() != null ? product.getId().value() : null,
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStockQuantity(),
                product.isActive()
        );
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

}