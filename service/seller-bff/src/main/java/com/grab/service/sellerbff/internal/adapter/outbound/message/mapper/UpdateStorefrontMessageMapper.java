package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.account.merchant.MerchantStorefrontPort;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.UpdateStorefrontRecord;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class UpdateStorefrontMessageMapper {

    public abstract MerchantStorefrontPort.UpdateStorefrontMessage toMessage(UpdateStorefrontRecord record);

    public abstract StorefrontOutboundResult toResult(MerchantStorefrontPort.StorefrontMessageResult result);
}

