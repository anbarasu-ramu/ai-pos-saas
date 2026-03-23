package com.anbu.aipos.adapters.out.persistence.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findAllByTenantIdOrderByNameAsc(String tenantId);

    List<ProductEntity> findByTenantId(String tenantId);

    Optional<ProductEntity> findByIdAndTenantId(Long id, String tenantId);
}
