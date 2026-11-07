package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantStorefrontPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.UpdateStorefrontRecord;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateStorefrontMessageMapper {

    public abstract MerchantStorefrontPort.UpdateStorefrontMessage toMessage(UpdateStorefrontRecord record);

    public abstract StorefrontOutboundResult toResult(MerchantStorefrontPort.StorefrontMessageResult result);
}

