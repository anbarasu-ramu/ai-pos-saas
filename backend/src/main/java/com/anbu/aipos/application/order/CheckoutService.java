package com.anbu.aipos.application.order;


import com.anbu.aipos.application.exception.ProductNotFoundException;
import com.anbu.aipos.core.domain.order.PosOrder;
import com.anbu.aipos.core.domain.order.PosOrderItem;
import com.anbu.aipos.core.domain.product.Product;
import com.anbu.aipos.core.port.in.order.CheckoutUseCase;
import com.anbu.aipos.core.port.out.OrderRepository;
import com.anbu.aipos.core.port.out.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CheckoutService implements CheckoutUseCase {
    private  final ProductRepository productRepo;
    private  final OrderRepository orderRepository;

    public CheckoutService(ProductRepository productRepo,OrderRepository orderRepository){
        this.productRepo = productRepo;
        this.orderRepository = orderRepository;
    }

    @Transactional
    @Override
    public CheckoutResponse checkout(CheckoutCommand command) {

        // 🔹 1. Create aggregate
        PosOrder order = PosOrder.create(command.tenantId());

        List<Product> productsToSave = new ArrayList<>();

        // 🔹 2. Process items
        for (var item : command.items()) {

            var product = productRepo.findByIdAndTenantId(
                    item.productId(), command.tenantId()
            ).orElseThrow(() -> new ProductNotFoundException(item.productId()));

            // domain logic
            product.reduceStock(item.quantity());

            order.addItem(
                    product.getId().value(),
                    product.getName(),
                    item.quantity(),
                    product.getPrice()
            );

            productsToSave.add(product);
        }

        // 🔹 3. Complete order (ALL business rules inside domain)
        order.complete(command.paymentType(), command.amountPaid());

        // 🔹 4. Persist (AFTER all validations pass)
        productRepo.saveAll(productsToSave);
        order = orderRepository.save(order);

        // 🔹 5. Calculate change (can also move to domain later)
        BigDecimal change = command.amountPaid().subtract(order.getTotalAmount());

        // 🔹 6. Build response WITHOUT extra DB call
        return new CheckoutResponse(
                order.getId(),
                order.getTotalAmount(),
                change,
                order.getStatus(),
                mapItems(order.getItems())
        );
    }

    private List<CheckoutItem> mapItems(List<PosOrderItem> items) {
        return items.stream().map(item -> new CheckoutItem(item.getProductId(),item.getName(),item.getQuantity(),item.getUnitPrice())).collect(Collectors.toList());
    }


}
