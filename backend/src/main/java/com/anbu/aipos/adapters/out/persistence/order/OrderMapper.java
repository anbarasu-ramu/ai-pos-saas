package com.anbu.aipos.adapters.out.persistence.order;


import com.anbu.aipos.core.domain.order.PosOrder;
import com.anbu.aipos.core.domain.order.PosOrderItem;


public class OrderMapper {

    public static PosOrderEntity toEntity(PosOrder order) {
        PosOrderEntity entity = new PosOrderEntity();
        entity.setTotalAmount(order.getTotalAmount());
        entity.setStatus(order.getStatus());
        entity.setTenantId(order.getTenantId());
        return entity;
    }

//    public static PosOrder toDomain(PosOrderEntity entity) {
//        return  PosOrder.restore(entity.getId(),entity.getTotalAmount(), entity.getStatus());
//    }

//    public static PosOrderItemEntity toItemEntity(PosOrderItem item, Long orderId) {
//        PosOrderItemEntity entity = new PosOrderItemEntity();
//        entity.setOrderId(orderId);
//        entity.setProductId(item.getProductId());
//        entity.setQuantity(item.getQuantity());
//        entity.setUnitPrice(item.getUnitPrice());
//        return entity;
//    }

    public static PosOrderItemEntity toItemEntity(PosOrderItem item, PosOrderEntity order) {
        PosOrderItemEntity entity = new PosOrderItemEntity();
        entity.setOrder(order);  // 🔥 important
        entity.setProductId(item.getProductId());
        entity.setQuantity(item.getQuantity());
        entity.setUnitPrice(item.getUnitPrice());
        return entity;
    }

//    public static List<PosOrderItem> toDomainItems(List<PosOrderItemEntity> entities, Map<Long, String> productNamesById){
//        return entities.stream().map(e -> toDomainItem(e, productNamesById) ).toList();
//    }
//
//    public static PosOrderItem toDomainItem(PosOrderItemEntity entity,Map<Long, String> productNamesById){
//        return  PosOrderItem.restore(
//                entity.getOrderId(), productNamesById.get(entity.getProductId()), entity.getProductId(),entity.getQuantity(),entity.getUnitPrice()
//        );
//    }
//
//    public static PosOrderItem toDomainItem(PosOrderItemEntity entity, String name){
//        return  PosOrderItem.restore(
//                entity.getOrderId(), null, entity.getProductId(),entity.getQuantity(),entity.getUnitPrice()
//        );
//    }
}