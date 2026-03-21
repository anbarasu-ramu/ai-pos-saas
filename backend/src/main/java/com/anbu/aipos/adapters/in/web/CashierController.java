package com.anbu.aipos.adapters.in.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cashier")
public class CashierController {

    @GetMapping("/only")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'CASHIER')")
    public String cashierAccess() {
        return "Hello Cashier/Admin!";
    }
}
