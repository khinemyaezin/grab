package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateInventoryItemRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.InventoryItemResponse;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.CreateSellerInventoryItemCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.InventoryItemOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class CreateSellerInventoryItemMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "actorId", source = "actorId")
    @Mapping(target = "sku", source = "request.sku")
    @Mapping(target = "locationId", source = "request.locationId")
    @Mapping(target = "initialQuantity", source = "request.initialQuantity")
    @Mapping(target = "safetyStock", source = "request.safetyStock")
    @Mapping(target = "reorderPoint", source = "request.reorderPoint")
    @Mapping(target = "reorderQuantity", source = "request.reorderQuantity")
    @Mapping(target = "maxStock", source = "request.maxStock")
    public abstract CreateSellerInventoryItemCommand toCommand(String merchantId, String actorId, CreateInventoryItemRequest request);

    public abstract InventoryItemResponse toResponse(InventoryItemOutboundResult result);
}

