package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.CreateFacilityLocationRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.FacilityLocationResponse;
import com.grab.app.platform.seller.internal.command.model.SellerInventoryCommands.CreateSellerLocationCommand;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityLocationOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class CreateSellerLocationMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "actorId", source = "actorId")
    @Mapping(target = "code", source = "request.code")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "type", source = "request.type")
    @Mapping(target = "line1", expression = "java(request.resolveLine1())")
    @Mapping(target = "line2", expression = "java(request.resolveLine2())")
    @Mapping(target = "city", expression = "java(request.resolveCity())")
    @Mapping(target = "state", expression = "java(request.resolveState())")
    @Mapping(target = "postalCode", expression = "java(request.resolvePostalCode())")
    @Mapping(target = "country", expression = "java(request.resolveCountry())")
    public abstract CreateSellerLocationCommand toCommand(String merchantId, String actorId, CreateFacilityLocationRequest request);

    public abstract FacilityLocationResponse toResponse(FacilityLocationOutboundResult result);
}
