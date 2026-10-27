package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.ReserveStockRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.InventoryReservationResponse;
import com.inventory.application.model.write.InventoryReservationResult;
import com.inventory.application.model.write.ReserveStockCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ReserveStockRequestMapper {

    public abstract ReserveStockCommand toCommand(String inventoryItemId, ReserveStockRequest request, String idempotencyKey, String createdBy, String scopeKey, String scopeId);

    public abstract InventoryReservationResponse toResponse(InventoryReservationResult result);
}
