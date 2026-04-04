package com.anbu.aipos.adapters.out.persistence.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemJpaRepository extends JpaRepository<PosOrderItemEntity, Long> {
    @Query("""
            select item
            from PosOrderItemEntity item
            where item.order.id = :orderId
              and item.order.tenantId = :tenantId
            order by item.id asc
            """)
    List<PosOrderItemEntity> findAllByOrderIdAndTenantId(Long orderId, String tenantId);
}
