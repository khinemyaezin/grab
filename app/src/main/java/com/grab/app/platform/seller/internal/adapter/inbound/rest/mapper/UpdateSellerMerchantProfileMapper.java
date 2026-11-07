package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request.UpdateMerchantProfileRequest;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.MerchantResponse;
import com.grab.app.platform.seller.internal.command.model.SellerMerchantCommands.UpdateSellerMerchantProfileCommand;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class UpdateSellerMerchantProfileMapper {

    @Mapping(target = "merchantId", source = "merchantId")
    @Mapping(target = "actorId", source = "actorId")
    @Mapping(target = "legalName", source = "request.legalName")
    @Mapping(target = "displayName", source = "request.displayName")
    @Mapping(target = "registrationCountryCode", source = "request.registrationCountryCode")
    @Mapping(target = "registrationNumber", source = "request.registrationNumber")
    @Mapping(target = "contactEmail", source = "request.contactEmail")
    @Mapping(target = "contactPhone", source = "request.contactPhone")
    @Mapping(target = "addressLine1", source = "request.addressLine1")
    @Mapping(target = "addressLine2", source = "request.addressLine2")
    @Mapping(target = "addressCity", source = "request.addressCity")
    @Mapping(target = "addressRegion", source = "request.addressRegion")
    @Mapping(target = "addressPostalCode", source = "request.addressPostalCode")
    @Mapping(target = "addressCountryCode", source = "request.addressCountryCode")
    public abstract UpdateSellerMerchantProfileCommand toCommand(String merchantId, String actorId, UpdateMerchantProfileRequest request);

    public abstract MerchantResponse toResponse(MerchantOutboundResult result);
}

