package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.TransferInventoryRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.InventoryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.TransferInventoryResponse;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.TransferInventoryCommand;
import com.inventory.application.model.write.TransferInventoryResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class TransferInventoryRequestMapper {

    public abstract TransferInventoryCommand toCommand(
            String inventoryItemId,
            TransferInventoryRequest request,
            String createdBy,
            String scopeKey,
            String scopeId
    );

    @Mapping(target = "source", source = "source")
    @Mapping(target = "destination", source = "destination")
    @Mapping(target = "transferId", source = "transferId")
    public abstract TransferInventoryResponse toResponse(TransferInventoryResult result);

    public abstract InventoryResponse toInventoryResponse(InventoryItemResult result);
}
