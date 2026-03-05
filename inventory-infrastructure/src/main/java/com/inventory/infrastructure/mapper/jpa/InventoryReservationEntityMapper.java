package com.inventory.infrastructure.mapper.jpa;

import com.grab.framework.mapper.IdMapper;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.infrastructure.entity.InventoryReservationEntity;
import com.inventory.infrastructure.entity.meta.InventoryReservationEntity_;
import com.inventory.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMapperConfig.class, uses = {IdMapper.class})
public abstract class InventoryReservationEntityMapper {

    @Mapping(ignore = true, target = InventoryReservationEntity_.ID)
    @Mapping(source = "id", target = InventoryReservationEntity_.UUID)
    @Mapping(source = "inventoryItemId", target = InventoryReservationEntity_.INVENTORY_ITEM_UUID)
    @Mapping(source = "orderId", target = InventoryReservationEntity_.ORDER_ID)
    @Mapping(source = "orderLineId", target = InventoryReservationEntity_.ORDER_LINE_ID)
    @Mapping(source = "quantity", target = InventoryReservationEntity_.QUANTITY)
    @Mapping(source = "status", target = InventoryReservationEntity_.STATUS)
    @Mapping(source = "expiresAt", target = InventoryReservationEntity_.EXPIRES_AT)
    @Mapping(source = "idempotencyKey", target = InventoryReservationEntity_.IDEMPOTENCY_KEY)
    @Mapping(source = "createdAt", target = InventoryReservationEntity_.CREATED_AT)
    @Mapping(source = "updatedAt", target = InventoryReservationEntity_.UPDATED_AT)
    public abstract void toEntity(InventoryReservation source, @MappingTarget InventoryReservationEntity destination);
}
