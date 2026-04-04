package com.anbu.aipos.adapters.in.web.dto.product;

import com.anbu.aipos.core.port.in.product.ProductView;
import java.util.List;

public record ProductSearchResponse(
        String query,
        int limit,
        long matchCount,
        List<ProductView> products
) {
}
