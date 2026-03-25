package com.anbu.aipos.core.port.out;

import com.anbu.aipos.core.domain.product.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    List<Product> findByTenantId(String tenantId);

    Optional<Product> findByIdAndTenantId(Long id, String tenantId);

    void delete(Product product);

    void saveAll(List<Product> productsToSave);
}