package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantAccountPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.UpdateProfileRecord;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateMerchantProfileMessageMapper {

    @Mapping(target = "merchantId", source = "record.merchantId")
    @Mapping(target = "applicantUserId", source = "record.actorId")
    @Mapping(target = "legalName", source = "record.legalName")
    @Mapping(target = "displayName", source = "record.displayName")
    @Mapping(target = "registrationCountryCode", source = "record.registrationCountryCode")
    @Mapping(target = "registrationNumber", source = "record.registrationNumber")
    @Mapping(target = "contactEmail", source = "record.contactEmail")
    @Mapping(target = "contactPhone", source = "record.contactPhone")
    @Mapping(target = "addressLine1", source = "record.addressLine1")
    @Mapping(target = "addressLine2", source = "record.addressLine2")
    @Mapping(target = "addressCity", source = "record.addressCity")
    @Mapping(target = "addressRegion", source = "record.addressRegion")
    @Mapping(target = "addressPostalCode", source = "record.addressPostalCode")
    @Mapping(target = "addressCountryCode", source = "record.addressCountryCode")
    public abstract MerchantAccountPort.UpdateMerchantProfileMessage toMessage(UpdateProfileRecord record);

    public abstract MerchantOutboundResult toResult(MerchantAccountPort.MerchantMessageResult result);
}
