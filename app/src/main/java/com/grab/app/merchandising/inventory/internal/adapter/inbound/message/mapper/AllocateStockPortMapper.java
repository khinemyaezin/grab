package com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryAvailabilityPort.AllocateStockMessage;
import com.grab.app.merchandising.inventory.port.message.InventoryAvailabilityPort.AllocationMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.write.AllocateStockCommand;
import com.inventory.application.model.write.AllocateStockResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class AllocateStockPortMapper {

    public abstract AllocateStockCommand toCommand(AllocateStockMessage message);

    public abstract AllocationMessageResult toResponse(AllocateStockResult result);
}

