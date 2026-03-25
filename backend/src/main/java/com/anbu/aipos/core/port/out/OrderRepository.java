package com.anbu.aipos.core.port.out;

import com.anbu.aipos.core.domain.order.PosOrder;

public interface OrderRepository {

    PosOrder save(PosOrder order);

//    List<PosOrderItem> findItemsByOrderId(Long id);

//    void saveItem(PosOrderItemEntity item);
}
