package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.AnnounceInTransitRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.InventoryResponse;
import com.inventory.application.model.write.AnnounceInTransitCommand;
import com.inventory.application.model.write.InventoryItemResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class AnnounceInTransitRequestMapper {

    public abstract AnnounceInTransitCommand toCommand(
            String inventoryItemId,
            AnnounceInTransitRequest request,
            String createdBy,
            String scopeKey,
            String scopeId
    );

    public abstract InventoryResponse toResponse(InventoryItemResult result);
}
