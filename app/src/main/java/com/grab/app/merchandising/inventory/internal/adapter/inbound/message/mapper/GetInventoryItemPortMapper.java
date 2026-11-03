package com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryItemPort.ItemMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.read.GetInventoryQuery;
import com.inventory.application.model.read.GetInventoryResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetInventoryItemPortMapper {

    public GetInventoryQuery toQuery(Id itemId) {
        return itemId != null ? new GetInventoryQuery(itemId) : null;
    }

    public abstract ItemMessageResult toResponse(GetInventoryResult result);
}

