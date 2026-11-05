package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.AllocateStockRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.AllocationResponse;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.AllocateSellerStockCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.AllocationOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class AllocateSellerStockMapper {

    @Mapping(target = "actorId", source = "actorId")
    @Mapping(target = "sku", source = "request.sku")
    @Mapping(target = "locationId", source = "request.locationId")
    @Mapping(target = "quantity", source = "request.quantity")
    @Mapping(target = "orderId", source = "request.orderId")
    public abstract AllocateSellerStockCommand toCommand(String actorId, AllocateStockRequest request);

    public abstract AllocationResponse toResponse(AllocationOutboundResult result);
}

