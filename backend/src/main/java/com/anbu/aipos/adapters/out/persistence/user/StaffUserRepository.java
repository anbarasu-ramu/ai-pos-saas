package com.anbu.aipos.adapters.out.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StaffUserRepository extends JpaRepository<StaffUserEntity, UUID> {

    Optional<StaffUserEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByIdAndTenantId(UUID id, UUID tenantId);
}