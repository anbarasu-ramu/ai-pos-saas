package com.anbu.aipos.adapters.in.web.user;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/only")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public String adminOnly() {
        return "Hello Admin!";
    }
}