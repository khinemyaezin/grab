package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantAccountPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListMyMerchantsMessageMapper {

    public abstract List<MerchantOutboundResult> toResultList(List<MerchantAccountPort.MerchantMessageResult> results);

    public abstract MerchantOutboundResult toResult(MerchantAccountPort.MerchantMessageResult result);
}

