package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateFacilityZoneRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityZoneResponse;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.CreateSellerZoneCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityZoneOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class CreateSellerZoneMapper {

    @Mapping(target = "locationId", source = "locationId")
    @Mapping(target = "actorId", source = "actorId")
    @Mapping(target = "code", source = "request.code")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "type", source = "request.type")
    public abstract CreateSellerZoneCommand toCommand(String locationId, String actorId, CreateFacilityZoneRequest request);

    public abstract FacilityZoneResponse toResponse(FacilityZoneOutboundResult result);
}

