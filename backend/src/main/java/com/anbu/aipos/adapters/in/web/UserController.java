package com.anbu.aipos.adapters.in.web;

import com.anbu.aipos.adapters.in.web.dto.user.CreateUserRequest;
import com.anbu.aipos.application.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {

        String tenantId = jwt.getClaim("tenant_id");

        userService.createUser(request, tenantId);

        return ResponseEntity.ok("User created successfully");
    }
}
