package com.inventory.infrastructure.mapper.jpa;

import com.inventory.domain.entity.StockMovement;
import com.inventory.infrastructure.entity.StockMovementEntity;
import com.inventory.infrastructure.entity.meta.StockMovementEntity_;
import com.inventory.infrastructure.mapper.CentralMapperConfig;
import com.inventory.infrastructure.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {IdMapper.class})
public abstract class StockMovementEntityMapper {

    @Mapping(ignore = true, target = StockMovementEntity_.ID)
    @Mapping(source = "id", target = StockMovementEntity_.UUID)
    @Mapping(source = "inventoryItemId", target = StockMovementEntity_.INVENTORY_ITEM_UUID)
    @Mapping(source = "type", target = StockMovementEntity_.TYPE)
    @Mapping(source = "quantity", target = StockMovementEntity_.QUANTITY)
    @Mapping(source = "quantityBefore", target = StockMovementEntity_.QUANTITY_BEFORE)
    @Mapping(source = "quantityAfter", target = StockMovementEntity_.QUANTITY_AFTER)
    @Mapping(source = "referenceId", target = StockMovementEntity_.REFERENCE_ID)
    @Mapping(source = "createdAt", target = StockMovementEntity_.CREATED_AT)
    @Mapping(source = "createdBy", target = StockMovementEntity_.CREATED_BY)
    public abstract void toEntity(StockMovement source, @MappingTarget StockMovementEntity destination);
}
