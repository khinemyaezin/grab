package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.CreateInventoryRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.InventoryResponse;
import com.inventory.application.model.write.CreateInventoryCommand;
import com.inventory.application.model.write.InventoryItemResult;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateInventoryRequestMapper {

    public abstract CreateInventoryCommand toCommand(CreateInventoryRequest request, String merchantId, String createdBy, String scopeKey, String scopeId);

    public abstract InventoryResponse toResponse(InventoryItemResult result);
}
