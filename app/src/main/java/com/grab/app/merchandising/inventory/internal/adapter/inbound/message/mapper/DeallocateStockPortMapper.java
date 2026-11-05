package com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryAvailabilityPort.DeallocateStockMessage;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.write.DeallocateStockCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class DeallocateStockPortMapper {

    public abstract DeallocateStockCommand toCommand(DeallocateStockMessage message);
}

