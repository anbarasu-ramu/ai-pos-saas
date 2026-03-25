package com.anbu.aipos.adapters.out.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJpaRepository extends JpaRepository<PosOrderEntity, Long> {
}
