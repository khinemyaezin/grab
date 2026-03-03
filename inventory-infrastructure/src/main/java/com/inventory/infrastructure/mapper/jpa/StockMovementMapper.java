package com.inventory.infrastructure.mapper.jpa;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.entity.StockMovement;
import com.inventory.infrastructure.entity.StockMovementEntity;
import com.inventory.infrastructure.entity.meta.StockMovementEntity_;
import com.inventory.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = {IdGenerator.class})
public abstract class StockMovementMapper {

    @Mapping(source = "entity." + StockMovementEntity_.UUID, target = "id")
    @Mapping(source = "entity." + StockMovementEntity_.INVENTORY_ITEM_UUID, target = "inventoryItemId")
    @Mapping(source = "entity." + StockMovementEntity_.TYPE, target = "type")
    @Mapping(source = "entity." + StockMovementEntity_.QUANTITY, target = "quantity")
    @Mapping(source = "entity." + StockMovementEntity_.QUANTITY_BEFORE, target = "quantityBefore")
    @Mapping(source = "entity." + StockMovementEntity_.QUANTITY_AFTER, target = "quantityAfter")
    @Mapping(source = "entity." + StockMovementEntity_.REFERENCE_ID, target = "referenceId")
    @Mapping(source = "entity." + StockMovementEntity_.CREATED_AT, target = "createdAt")
    @Mapping(source = "entity." + StockMovementEntity_.CREATED_BY, target = "createdBy")
    public abstract StockMovement toDomain(StockMovementEntity entity);
}
