package com.anbu.aipos.core.port.out;

import com.anbu.aipos.core.domain.product.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    boolean existsByTenantIdAndNameIgnoreCase(String tenantId, String name);

    List<Product> findByTenantId(String tenantId);

    Optional<Product> findByIdAndTenantId(Long id, String tenantId);

    List<Product> search(String tenantId, String query, boolean activeOnly, int limit);

    List<Product> findLowStockProducts(String tenantId, int threshold);

    void delete(Product product);

    void saveAll(List<Product> productsToSave);
}
