package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import com.grab.store.inventory.internal.command.InventoryReservationResult;
import com.grab.store.inventory.internal.command.ShipReservationCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ShipReservationRequestMapper {

    public abstract ShipReservationCommand toCommand(String inventoryItemId, String reservationId, String createdBy);

    public abstract InventoryReservationResponse toResponse(InventoryReservationResult result);
}
