package com.inventory.infrastructure.mapper.jpa;

import com.grab.framework.mapper.IdMapper;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.infrastructure.entity.InventoryItemEntity;
import com.inventory.infrastructure.entity.meta.InventoryItemEntity_;
import com.inventory.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {IdMapper.class})
public abstract class InventoryItemEntityMapper {

    @Mapping(ignore = true, target = InventoryItemEntity_.ID)
    @Mapping(source = "id", target = InventoryItemEntity_.UUID)
    @Mapping(source = "sku", target = InventoryItemEntity_.SKU)
    @Mapping(source = "sellerId", target = InventoryItemEntity_.SELLER_ID)
    @Mapping(source = "productVariantId", target = InventoryItemEntity_.PRODUCT_VARIANT_ID)
    @Mapping(source = "locationId", target = InventoryItemEntity_.LOCATION_ID)
    @Mapping(source = "quantity.onHand", target = InventoryItemEntity_.ON_HAND)
    @Mapping(source = "quantity.reserved", target = InventoryItemEntity_.RESERVED)
    @Mapping(source = "quantity.inTransit", target = InventoryItemEntity_.IN_TRANSIT)
    @Mapping(source = "quantity.damaged", target = InventoryItemEntity_.DAMAGED)
    @Mapping(source = "reorderConfig.safetyStock", target = InventoryItemEntity_.SAFETY_STOCK)
    @Mapping(source = "reorderConfig.reorderPoint", target = InventoryItemEntity_.REORDER_POINT)
    @Mapping(source = "reorderConfig.reorderQuantity", target = InventoryItemEntity_.REORDER_QUANTITY)
    @Mapping(source = "reorderConfig.maxStock", target = InventoryItemEntity_.MAX_STOCK)
    @Mapping(source = "status", target = InventoryItemEntity_.STATUS)
    @Mapping(ignore = true, target = InventoryItemEntity_.LAST_UPDATED)
    @Mapping(ignore = true, target = InventoryItemEntity_.VERSION)
    public abstract void toEntity(InventoryItem source, @MappingTarget InventoryItemEntity destination);
}
