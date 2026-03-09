package com.inventory.infrastructure.mapper.jpa;

import com.grab.framework.id.IdGenerator;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.infrastructure.entity.InventoryReservationEntity;
import com.inventory.infrastructure.entity.meta.InventoryReservationEntity_;
import com.inventory.infrastructure.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = {IdGenerator.class})
public abstract class InventoryReservationMapper {

    @Mapping(source = "entity." + InventoryReservationEntity_.UUID, target = "id")
    @Mapping(source = "entity." + InventoryReservationEntity_.INVENTORY_ITEM_UUID, target = "inventoryItemId")
    @Mapping(source = "entity." + InventoryReservationEntity_.ORDER_ID, target = "orderId")
    @Mapping(source = "entity." + InventoryReservationEntity_.ORDER_LINE_ID, target = "orderLineId")
    @Mapping(source = "entity." + InventoryReservationEntity_.QUANTITY, target = "quantity")
    @Mapping(source = "entity." + InventoryReservationEntity_.STATUS, target = "status")
    @Mapping(source = "entity." + InventoryReservationEntity_.EXPIRES_AT, target = "expiresAt")
    @Mapping(source = "entity." + InventoryReservationEntity_.IDEMPOTENCY_KEY, target = "idempotencyKey")
    @Mapping(source = "entity." + InventoryReservationEntity_.CREATED_AT, target = "createdAt")
    @Mapping(source = "entity." + InventoryReservationEntity_.UPDATED_AT, target = "updatedAt")
    public abstract InventoryReservation toDomain(InventoryReservationEntity entity);
}
