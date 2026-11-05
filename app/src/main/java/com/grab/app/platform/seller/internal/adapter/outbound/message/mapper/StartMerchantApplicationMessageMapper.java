package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantAccountPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.StartApplicationRecord;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class StartMerchantApplicationMessageMapper {

    @Mapping(target = "applicantUserId", source = "record.actorId")
    @Mapping(target = "businessType", source = "record.businessType")
    @Mapping(target = "displayName", source = "record.displayName")
    @Mapping(target = "primaryMarketCountryCode", source = "record.primaryMarketCountryCode")
    public abstract MerchantAccountPort.StartMerchantApplicationMessage toMessage(StartApplicationRecord record);

    public abstract MerchantOutboundResult toResult(MerchantAccountPort.MerchantMessageResult result);
}
