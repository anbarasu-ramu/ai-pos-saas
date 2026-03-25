package com.anbu.aipos.adapters.out.persistence.order;

import com.anbu.aipos.adapters.out.persistence.product.ProductJpaRepository;
import com.anbu.aipos.core.domain.order.PosOrder;
import com.anbu.aipos.core.port.out.OrderRepository;
import org.springframework.stereotype.Component;

@Component
public class OrderRepositoryAdapter implements OrderRepository {
    private final OrderJpaRepository orderRepo;
    private final OrderItemJpaRepository itemRepo;
    private final ProductJpaRepository productRepo;

    OrderRepositoryAdapter(OrderJpaRepository orderRepo,OrderItemJpaRepository itemRepo,ProductJpaRepository productRepo){
            this.orderRepo = orderRepo;
            this.itemRepo = itemRepo;
            this.productRepo = productRepo;
    }

    @Override
    public PosOrder save(PosOrder order) {

        // 🔹 Save order
        var orderEntity = OrderMapper.toEntity(order);
        orderEntity = orderRepo.save(orderEntity);

        // 🔹 Save items
        for (var item : order.getItems()) {
            var itemEntity = OrderMapper.toItemEntity(item, orderEntity);
            itemRepo.save(itemEntity);
        }

        // 🔹 Update domain with ID
        order.assignId(orderEntity.getId());


        return order;
    }

//    @Override
//    public List<PosOrderItem> findItemsByOrderId(Long id) {
//       var orderItems = itemRepo.findAll(id);
//       var productNamesById = this.productRepo.findById(orderItems.stream().map(PosOrderItemEntity::getProductId).toList())
//                .stream().collect(Collectors.toMap(ProductEntity::getId, ProductEntity::getName));
//        return OrderMapper.toDomainItems(itemRepo.findAll(id), productNamesById);
//    }
}
