package com.anbu.aipos.product.service;

import com.anbu.aipos.product.domain.Product;
import com.anbu.aipos.product.repository.ProductRepository;
import com.anbu.aipos.tenant.service.TenantContextService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final TenantContextService tenantContextService;

    public ProductService(ProductRepository productRepository, TenantContextService tenantContextService) {
        this.productRepository = productRepository;
        this.tenantContextService = tenantContextService;
    }

    public List<Product> listProducts() {
        return tenantContextService.currentTenantId()
                .map(productRepository::findAllByTenantIdOrderByNameAsc)
                .orElseGet(productRepository::findAll);
    }
}
