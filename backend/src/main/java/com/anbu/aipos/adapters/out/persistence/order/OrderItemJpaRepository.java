package com.anbu.aipos.adapters.out.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemJpaRepository extends JpaRepository<PosOrderItemEntity, Long> {
    List<PosOrderItemEntity> findAllByOrderId(Long orderId);
}
