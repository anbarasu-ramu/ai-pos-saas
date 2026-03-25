package com.anbu.aipos.adapters.out.persistence.tenant.repository;

import com.anbu.aipos.adapters.out.persistence.tenant.domain.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface TenantJpaRepository extends JpaRepository<TenantEntity, Long> {

    boolean existsBySlug(String slug);

    boolean existsByNameIgnoreCase(String name);

    Optional<TenantEntity> findBySlug(String slug);

    @Query("select t.name from TenantEntity t where t.tenantId = :tenantId")
    Optional<String> findNameByTenantId(UUID tenantId);
}
