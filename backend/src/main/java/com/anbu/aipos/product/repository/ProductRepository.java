package com.anbu.aipos.product.repository;

import com.anbu.aipos.product.domain.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findAllByTenantIdOrderByNameAsc(String tenantId);
}
