package com.grab.service.merchandising.inventory.internal.adapter.inbound.message;

import com.grab.service.merchandising.inventory.internal.adapter.inbound.message.mapper.CreateInventoryItemPortMapper;
import com.grab.service.merchandising.inventory.internal.adapter.inbound.message.mapper.GetInventoryItemPortMapper;
import com.grab.service.merchandising.inventory.internal.adapter.inbound.message.mapper.GetInventoryMovementPortMapper;
import com.grab.service.contract.message.merchandising.inventory.InventoryItemPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.inventory.application.model.read.GetInventoryMovementsQuery;
import com.inventory.application.model.read.GetInventoryMovementsResult;
import com.inventory.application.model.read.GetInventoryQuery;
import com.inventory.application.model.read.GetInventoryResult;
import com.inventory.application.model.write.CreateInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import com.grab.rpc.adapter.RmiExport;

import java.util.Optional;

@Component
@RmiExport
public class InventoryItemPortAdapter implements InventoryItemPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final CreateInventoryItemPortMapper createItemMapper;
    private final GetInventoryItemPortMapper getItemMapper;
    private final GetInventoryMovementPortMapper getMovementMapper;

    public InventoryItemPortAdapter(
            @Qualifier("merchandisingCommandBus") CommandBus commandBus,
            @Qualifier("merchandisingQueryBus") QueryBus queryBus,
            CreateInventoryItemPortMapper createItemMapper,
            GetInventoryItemPortMapper getItemMapper,
            GetInventoryMovementPortMapper getMovementMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.createItemMapper = createItemMapper;
        this.getItemMapper = getItemMapper;
        this.getMovementMapper = getMovementMapper;
    }

    @Override
    public ItemMessageResult createInventoryItem(CreateInventoryItemMessage message) {
        CreateInventoryCommand command = createItemMapper.toCommand(message);
        InventoryItemResult result = commandBus.dispatch(command);
        return createItemMapper.toResponse(result);
    }

    @Override
    public ItemMessageResult findItemById(Id itemId) {
        if (itemId == null) {
            return null;
        }
        GetInventoryQuery query = getItemMapper.toQuery(itemId);
        GetInventoryResult result = queryBus.dispatch(query);
        return getItemMapper.toResponse(result);
    }

    @Override
    public MovementMessageResult findLatestMovement(Id itemId) {
        if (itemId == null) {
            return null;
        }
        GetInventoryMovementsQuery query = getMovementMapper.toQuery(itemId);
        Page<GetInventoryMovementsResult> results = queryBus.dispatch(query);
        if (results == null || results.isEmpty()) {
            return null;
        }
        return getMovementMapper.toResponse(results.getContent().getFirst());
    }
}
