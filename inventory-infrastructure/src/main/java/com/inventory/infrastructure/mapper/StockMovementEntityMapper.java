package com.inventory.infrastructure.mapper;

import com.grab.framework.mapper.CommonMapper;
import com.inventory.domain.entity.StockMovement;
import com.inventory.infrastructure.entity.StockMovementEntity;
import com.inventory.infrastructure.entity.meta.StockMovementEntity_;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = {CommonMapper.class})
public interface StockMovementEntityMapper {

    @Mapping(target = StockMovementEntity_.ID, ignore = true)
    @Mapping(target = StockMovementEntity_.UUID, source = "id")
    @Mapping(target = StockMovementEntity_.CREATED_BY, source = "createdBy")
    @Mapping(target = "inventoryItem", ignore = true)
    StockMovementEntity toEntity(StockMovement domain);


}
