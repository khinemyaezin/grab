package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateFacilityBinRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityBinResponse;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.CreateSellerBinCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityBinOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class CreateSellerBinMapper {

    @Mapping(target = "zoneId", source = "zoneId")
    @Mapping(target = "actorId", source = "actorId")
    @Mapping(target = "code", source = "request.code")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "maxCapacity", source = "request.maxCapacity")
    public abstract CreateSellerBinCommand toCommand(String zoneId, String actorId, CreateFacilityBinRequest request);

    public abstract FacilityBinResponse toResponse(FacilityBinOutboundResult result);
}

