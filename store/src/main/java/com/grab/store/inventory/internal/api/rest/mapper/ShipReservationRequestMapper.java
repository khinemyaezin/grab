package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import com.inventory.application.model.write.InventoryReservationResult;
import com.inventory.application.model.write.ShipReservationCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ShipReservationRequestMapper {

    public abstract ShipReservationCommand toCommand(String inventoryItemId, String reservationId, String createdBy, String scopeKey, String scopeId);

    public abstract InventoryReservationResponse toResponse(InventoryReservationResult result);
}
