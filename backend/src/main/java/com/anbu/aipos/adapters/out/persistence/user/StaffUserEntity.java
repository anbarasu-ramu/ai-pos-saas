package com.anbu.aipos.adapters.out.persistence.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "staff_user",
       uniqueConstraints = @UniqueConstraint(columnNames = {"id", "tenant_id"}))
public class StaffUserEntity {

    @Id
    private UUID id; // Keycloak sub

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    private String username;
    private String email;

    private String role;

    private boolean active = true;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    // getters/setters
}
