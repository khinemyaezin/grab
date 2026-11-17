package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.account.merchant.MerchantAccountPort;
import com.grab.service.sellerbff.port.outbound.SellerMerchantOutboundPort.MerchantOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class ListMyMerchantsMessageMapper {

    public abstract List<MerchantOutboundResult> toResultList(List<MerchantAccountPort.MerchantMessageResult> results);

    public abstract MerchantOutboundResult toResult(MerchantAccountPort.MerchantMessageResult result);
}

