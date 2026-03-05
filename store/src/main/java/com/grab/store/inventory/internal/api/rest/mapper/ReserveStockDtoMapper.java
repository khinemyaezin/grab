package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.ReserveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import com.grab.store.inventory.internal.command.InventoryReservationResult;
import com.grab.store.inventory.internal.command.ReserveStockCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ReserveStockDtoMapper {

    @Mapping(target = "idempotencyKey", source = "idempotencyKey")
    @Mapping(target = "createdBy", source = "actorId")
    public abstract ReserveStockCommand toCommand(
            String inventoryItemId,
            ReserveStockRequest request,
            String idempotencyKey,
            String actorId
    );

    public abstract InventoryReservationResponse toResponse(InventoryReservationResult result);
}
