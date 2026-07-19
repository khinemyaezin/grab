package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.TransferInventoryRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.TransferInventoryResponse;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.TransferInventoryCommand;
import com.grab.store.inventory.internal.command.TransferInventoryResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class TransferInventoryRequestMapper {

    public abstract TransferInventoryCommand toCommand(
            String inventoryItemId,
            TransferInventoryRequest request,
            String createdBy,
            String scopeKey,
            String scopeId
    );

    @Mapping(target = "source", source = "source")
    @Mapping(target = "destination", source = "destination")
    @Mapping(target = "transferId", source = "transferId")
    public abstract TransferInventoryResponse toResponse(TransferInventoryResult result);

    public abstract InventoryResponse toInventoryResponse(InventoryItemResult result);
}
