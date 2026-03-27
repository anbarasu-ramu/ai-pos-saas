package com.anbu.aipos.adapters.in.web.test;

import com.anbu.aipos.config.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/tenant")
public class TenantController {

    @GetMapping("/current")
    public Map<String, String> currentTenant() {
        return Map.of(
                "tenant_id", TenantContext.get()
        );
    }
}
