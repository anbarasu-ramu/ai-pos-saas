package com.anbu.aipos.adapters.out.persistence.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<PosOrderEntity, Long> {
    Page<PosOrderEntity> findByTenantId(String tenantId, Pageable pageable);
}
