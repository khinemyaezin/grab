package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.ReceiveInTransitRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.ReceiveInTransitCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ReceiveInTransitRequestMapper {

    public abstract ReceiveInTransitCommand toCommand(
            String inventoryItemId,
            ReceiveInTransitRequest request,
            String createdBy,
            String scopeKey,
            String scopeId
    );

    public abstract InventoryResponse toResponse(InventoryItemResult result);
}
