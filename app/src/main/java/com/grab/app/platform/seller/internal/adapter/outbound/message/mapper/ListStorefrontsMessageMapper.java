package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantStorefrontPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.StorefrontOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListStorefrontsMessageMapper {

    public abstract List<StorefrontOutboundResult> toResultList(List<MerchantStorefrontPort.StorefrontMessageResult> results);

    public abstract StorefrontOutboundResult toResult(MerchantStorefrontPort.StorefrontMessageResult result);
}

