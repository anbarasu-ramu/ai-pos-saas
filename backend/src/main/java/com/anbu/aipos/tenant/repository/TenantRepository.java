package com.anbu.aipos.tenant.repository;

import com.anbu.aipos.tenant.domain.Tenant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    boolean existsBySlug(String slug);

    boolean existsByNameIgnoreCase(String name);

    Optional<Tenant> findBySlug(String slug);
}
