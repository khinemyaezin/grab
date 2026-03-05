package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import com.grab.store.inventory.internal.command.InventoryReservationResult;
import com.grab.store.inventory.internal.command.ShipReservationCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ShipReservationDtoMapper {

    @Mapping(target = "createdBy", source = "actorId")
    public abstract ShipReservationCommand toCommand(String inventoryItemId, String reservationId, String actorId);

    public abstract InventoryReservationResponse toResponse(InventoryReservationResult result);
}
