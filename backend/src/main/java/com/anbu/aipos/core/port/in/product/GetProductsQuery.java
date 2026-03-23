package com.anbu.aipos.core.port.in.product;

import java.util.List;

public interface GetProductsQuery {
    List<ProductView> getAll(String tenantId);


}
