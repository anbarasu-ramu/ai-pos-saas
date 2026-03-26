package com.anbu.aipos.application.user;

import com.anbu.aipos.adapters.out.persistence.user.StaffUserEntity;
import com.anbu.aipos.adapters.out.persistence.user.StaffUserRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
public class StaffUserService {

    private final StaffUserRepository repo;

    public StaffUserService(StaffUserRepository repo) {
        this.repo = repo;
    }

//    public void ensureUserExists(Jwt jwt) {
//        UUID userId = UUID.fromString(jwt.getSubject());
//        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
//
//        if (repository.existsByIdAndTenantId(userId, tenantId)) {
//            return;
//        }
//
//        StaffUserEntity user = new StaffUserEntity();
//        user.setId(userId);
//        user.setTenantId(tenantId);
//        user.setUsername(jwt.getClaimAsString("preferred_username"));
//        user.setEmail(jwt.getClaimAsString("email"));
//
//        // default role (can be improved later)
//        user.setRole("CASHIER");
//
//        repository.save(user);
//    }

    public void createStaffUser(String email, String userId,UUID tenantId, String role) {
        StaffUserEntity staffUser = new StaffUserEntity();
        staffUser.setId(UUID.fromString(userId));
        staffUser.setTenantId(tenantId);
        staffUser.setUsername(email.trim().toLowerCase(Locale.ROOT));
        staffUser.setEmail(email.trim().toLowerCase(Locale.ROOT));
        staffUser.setRole(role);
        repo.save(staffUser);
    }
}
