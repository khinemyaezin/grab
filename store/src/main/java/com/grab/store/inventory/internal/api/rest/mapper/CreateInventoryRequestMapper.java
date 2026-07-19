package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.CreateInventoryRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.command.CreateInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateInventoryRequestMapper {

    public abstract CreateInventoryCommand toCommand(CreateInventoryRequest request, String merchantId, String createdBy, String scopeKey, String scopeId);

    public abstract InventoryResponse toResponse(InventoryItemResult result);
}
