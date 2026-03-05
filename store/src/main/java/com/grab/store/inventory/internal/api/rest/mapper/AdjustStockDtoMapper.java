package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.AdjustStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.command.AdjustStockCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class AdjustStockDtoMapper {

    @Mapping(target = "createdBy", source = "actorId")
    public abstract AdjustStockCommand toCommand(String inventoryItemId, AdjustStockRequest request, String actorId);

    public abstract InventoryResponse toResponse(InventoryItemResult result);
}
