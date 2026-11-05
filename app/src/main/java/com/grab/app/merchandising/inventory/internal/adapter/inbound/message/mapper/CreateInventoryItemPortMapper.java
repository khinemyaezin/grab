package com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryItemPort.CreateInventoryItemMessage;
import com.grab.app.merchandising.inventory.port.message.InventoryItemPort.ItemMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.write.CreateInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateInventoryItemPortMapper {

    public abstract CreateInventoryCommand toCommand(CreateInventoryItemMessage message);

    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "locationCode", ignore = true)
    @Mapping(target = "locationName", ignore = true)
    public abstract ItemMessageResult toResponse(InventoryItemResult result);
}

