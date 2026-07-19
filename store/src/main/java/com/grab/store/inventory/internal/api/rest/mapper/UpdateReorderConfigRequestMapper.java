package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.UpdateReorderConfigRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.UpdateReorderConfigCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateReorderConfigRequestMapper {

    public abstract UpdateReorderConfigCommand toCommand(
            String inventoryItemId,
            UpdateReorderConfigRequest request,
            String createdBy,
            String scopeKey,
            String scopeId
    );

    public abstract InventoryResponse toResponse(InventoryItemResult result);
}
