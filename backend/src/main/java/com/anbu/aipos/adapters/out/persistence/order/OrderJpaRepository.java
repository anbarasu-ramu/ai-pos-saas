package com.anbu.aipos.adapters.out.persistence.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderJpaRepository extends JpaRepository<PosOrderEntity, Long> {
    Page<PosOrderEntity> findByTenantId(String tenantId, Pageable pageable);

    Optional<PosOrderEntity> findByIdAndTenantId(Long id, String tenantId);

    long countByTenantIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            String tenantId,
            Instant start,
            Instant end
    );

    long countByTenantIdAndStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            String tenantId,
            String status,
            Instant start,
            Instant end
    );

    @Query("""
            select coalesce(sum(o.totalAmount), 0)
            from PosOrderEntity o
            where o.tenantId = :tenantId
              and o.createdAt >= :start
              and o.createdAt < :end
            """)
    BigDecimal sumTotalAmountForPeriod(
            @Param("tenantId") String tenantId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}
