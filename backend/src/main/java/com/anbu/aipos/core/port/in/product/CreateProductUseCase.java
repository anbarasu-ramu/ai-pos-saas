package com.anbu.aipos.core.port.in.product;

import com.anbu.aipos.core.domain.product.Product;

public interface CreateProductUseCase {

    Product create(CreateProductCommand command);


}


