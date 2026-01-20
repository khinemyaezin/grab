package com.inventory.infrastructure.mapper;

import com.grab.framework.mapper.CommonMapper;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.entity.meta.InventoryItemEntity_;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = {StockMovementMapper.class, CommonMapper.class})
public abstract class InventoryItemEntityMapper {

    @Mapping(target = InventoryItemEntity_.ID, ignore = true)
    @Mapping(target = InventoryItemEntity_.UUID, source = "id")
    @Mapping(target = InventoryItemEntity_.PRODUCT_VARIANT_ID, source = "productVariantId")
    @Mapping(target = InventoryItemEntity_.LOCATION_ID, source = "locationId")
    @Mapping(target = InventoryItemEntity_.ON_HAND, source = "quantity.onHand")
    @Mapping(target = InventoryItemEntity_.RESERVED, source = "quantity.reserved")
    @Mapping(target = InventoryItemEntity_.IN_TRANSIT, source = "quantity.inTransit")
    @Mapping(target = InventoryItemEntity_.DAMAGED, source = "quantity.damaged")
    @Mapping(target = InventoryItemEntity_.SAFETY_STOCK, source = "reorderConfig.safetyStock")
    @Mapping(target = InventoryItemEntity_.REORDER_POINT, source = "reorderConfig.reorderPoint")
    @Mapping(target = InventoryItemEntity_.REORDER_QUANTITY, source = "reorderConfig.reorderQuantity")
    @Mapping(target = InventoryItemEntity_.MAX_STOCK, source = "reorderConfig.maxStock")
    @Mapping(target = InventoryItemEntity_.MOVEMENT_ENTITIES, ignore = true)
    public abstract InventoryItemEntity toEntity(InventoryItem domain);

}
