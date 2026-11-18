package com.grab.service.merchandising.inventory.internal.adapter.inbound.message.mapper;

import com.grab.service.contract.message.merchandising.inventory.InventoryAvailabilityPort.DeallocateStockMessage;
import com.grab.service.merchandising.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.write.DeallocateStockCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class DeallocateStockPortMapper {

    public abstract DeallocateStockCommand toCommand(DeallocateStockMessage message);
}

