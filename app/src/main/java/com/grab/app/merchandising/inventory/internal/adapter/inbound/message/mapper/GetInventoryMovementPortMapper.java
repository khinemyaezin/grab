package com.grab.app.merchandising.inventory.internal.adapter.inbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryItemPort.MovementMessageResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.id.Id;
import com.grab.framework.mapper.IdMapper;
import com.inventory.application.model.read.GetInventoryMovementsQuery;
import com.inventory.application.model.read.GetInventoryMovementsResult;
import org.mapstruct.Mapper;
import org.springframework.data.domain.PageRequest;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetInventoryMovementPortMapper {

    public GetInventoryMovementsQuery toQuery(Id itemId) {
        return new GetInventoryMovementsQuery(itemId, PageRequest.of(0, 1));
    }

    public abstract MovementMessageResult toResponse(GetInventoryMovementsResult result);
}

