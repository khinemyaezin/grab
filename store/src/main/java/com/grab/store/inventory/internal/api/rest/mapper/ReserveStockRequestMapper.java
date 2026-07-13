package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.ReserveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryReservationResponse;
import com.grab.store.inventory.internal.command.InventoryReservationResult;
import com.grab.store.inventory.internal.command.ReserveStockCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ReserveStockRequestMapper {

    public abstract ReserveStockCommand toCommand(String inventoryItemId, ReserveStockRequest request, String idempotencyKey, String createdBy, String scopeKey, String scopeId);

    public abstract InventoryReservationResponse toResponse(InventoryReservationResult result);
}
