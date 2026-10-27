package com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.mapper;

import com.grab.framework.mapper.IdMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.request.ReturnToVendorRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.inventory.rest.dto.response.InventoryResponse;
import com.inventory.application.model.write.InventoryItemResult;
import com.inventory.application.model.write.ReturnToVendorCommand;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ReturnToVendorRequestMapper {

    public abstract ReturnToVendorCommand toCommand(String inventoryItemId, ReturnToVendorRequest request, String createdBy, String scopeKey, String scopeId);

    public abstract InventoryResponse toResponse(InventoryItemResult result);
}
