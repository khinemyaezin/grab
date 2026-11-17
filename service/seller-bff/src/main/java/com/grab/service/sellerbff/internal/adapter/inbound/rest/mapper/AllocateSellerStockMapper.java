package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.request.AllocateStockRequest;
import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.AllocationResponse;
import com.grab.service.sellerbff.internal.command.model.SellerInventoryCommands.AllocateSellerStockCommand;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.AllocationOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
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

