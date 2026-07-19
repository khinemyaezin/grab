package com.grab.store.inventory.internal.api.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.store.inventory.internal.api.rest.dto.request.AllocateStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.request.DeallocateStockRequest;
import com.grab.store.inventory.internal.api.rest.dto.response.AllocateStockResponse;
import com.grab.store.inventory.internal.api.rest.dto.response.DeallocateStockResponse;
import com.grab.store.inventory.internal.command.AllocateStockCommand;
import com.grab.store.inventory.internal.command.AllocateStockResult;
import com.grab.store.inventory.internal.command.DeallocateStockCommand;
import com.grab.store.inventory.internal.command.DeallocateStockResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class AllocationRequestMapper {

    public abstract AllocateStockCommand toAllocateCommand(
            AllocateStockRequest request,
            String createdBy,
            String scopeKey,
            String scopeId
    );

    public abstract DeallocateStockCommand toDeallocateCommand(
            DeallocateStockRequest request,
            String createdBy,
            String scopeKey,
            String scopeId
    );

    public abstract AllocateStockResponse toAllocateResponse(AllocateStockResult result);

    public abstract DeallocateStockResponse toDeallocateResponse(DeallocateStockResult result);
}
