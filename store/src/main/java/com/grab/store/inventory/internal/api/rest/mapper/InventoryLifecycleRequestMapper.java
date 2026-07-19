package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.response.InventoryResponse;
import com.grab.store.inventory.internal.command.ActivateInventoryCommand;
import com.grab.store.inventory.internal.command.DiscontinueInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.SuspendInventoryCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class InventoryLifecycleRequestMapper {

    public abstract SuspendInventoryCommand toSuspendCommand(String inventoryItemId, String createdBy, String scopeKey, String scopeId);

    public abstract ActivateInventoryCommand toActivateCommand(String inventoryItemId, String createdBy, String scopeKey, String scopeId);

    public abstract DiscontinueInventoryCommand toDiscontinueCommand(String inventoryItemId, String createdBy, String scopeKey, String scopeId);

    public abstract InventoryResponse toResponse(InventoryItemResult result);
}
