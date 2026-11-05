package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantStorefrontPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.CreateStorefrontRecord;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateStorefrontMessageMapper {

    @Mapping(target = "merchantId", source = "record.merchantId")
    @Mapping(target = "name", source = "record.name")
    @Mapping(target = "slug", source = "record.slug")
    public abstract MerchantStorefrontPort.CreateStorefrontMessage toMessage(CreateStorefrontRecord record);

    public abstract StorefrontOutboundResult toResult(MerchantStorefrontPort.StorefrontMessageResult result);
}

