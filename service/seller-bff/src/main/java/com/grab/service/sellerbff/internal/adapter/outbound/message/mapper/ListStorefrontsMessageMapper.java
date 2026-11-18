package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.account.merchant.MerchantStorefrontPort;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListStorefrontsMessageMapper {

    public abstract List<StorefrontOutboundResult> toResultList(List<MerchantStorefrontPort.StorefrontMessageResult> results);

    public abstract StorefrontOutboundResult toResult(MerchantStorefrontPort.StorefrontMessageResult result);
}

