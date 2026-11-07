package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.DeallocateStockRequest;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.DeallocateSellerStockCommand;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class DeallocateSellerStockMapper {

    @Mapping(target = "actorId", source = "actorId")
    @Mapping(target = "sku", source = "request.sku")
    @Mapping(target = "quantity", source = "request.quantity")
    @Mapping(target = "orderId", source = "request.orderId")
    public abstract DeallocateSellerStockCommand toCommand(String actorId, DeallocateStockRequest request);
}

