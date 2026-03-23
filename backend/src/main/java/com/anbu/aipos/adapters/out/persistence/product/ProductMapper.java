package com.anbu.aipos.adapters.out.persistence.product;

import com.anbu.aipos.core.domain.product.Product;
import com.anbu.aipos.core.domain.product.ProductId;
import org.springframework.stereotype.Component;


@Component
public class ProductMapper {

    // 🔹 Entity → Domain
    public Product toDomain(ProductEntity entity) {
       return new Product(
                entity.getId() != null ? new ProductId(entity.getId()) : null,
               entity.getName(),
               entity.getCategory(),
               entity.getPrice(),
               entity.getStockQuantity(),
               entity.getActive(),
               entity.getTenantId()
       );
        // return null;
    }

    // 🔹 Domain → Entity
    public ProductEntity toEntity(Product product) {
        ProductEntity entity = new ProductEntity();

        if (product.getId() != null) {
            entity.setId(product.getId().value());
        }

        entity.setName(product.getName());
        entity.setCategory(product.getCategory());
        entity.setPrice(product.getPrice());
        entity.setStockQuantity(product.getStockQuantity());
        entity.setActive(product.isActive());
        entity.setTenantId(product.getTenantId());

        return entity;
    }
}