package com.anbu.aipos.adapters.out.persistence.product;

import com.anbu.aipos.core.domain.product.Product;
import com.anbu.aipos.core.port.out.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final ProductMapper mapper;

    public ProductRepositoryAdapter(ProductJpaRepository jpaRepository, ProductMapper mapper){
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        ProductEntity saved = jpaRepository.save(mapper.toEntity(product));
        return mapper.toDomain(saved);
    }

    @Override
    public List<Product> findByTenantId(String tenantId) {
        return jpaRepository.findByTenantId(tenantId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Product> findByIdAndTenantId(Long id, String tenantId) {
        return jpaRepository.findByIdAndTenantId(id, tenantId)
                .map(mapper::toDomain);
    }

    @Override
    public void delete(Product product) {
        if (product.getId() != null) {
            jpaRepository.deleteById(product.getId().value());
        }
    }
}