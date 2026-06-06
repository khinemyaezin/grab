package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.ReceiveStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.ReceiveStockCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ReceiveStockRequestMapper {

    public abstract ReceiveStockCommand toCommand(String inventoryItemId, ReceiveStockRequest request, String createdBy);

    public abstract InventoryResponse toResponse(InventoryItemResult result);
}
