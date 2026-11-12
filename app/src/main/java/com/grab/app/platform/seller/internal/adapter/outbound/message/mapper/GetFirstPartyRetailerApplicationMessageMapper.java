package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantAccountPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.FirstPartyRetailerApplicationOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetFirstPartyRetailerApplicationMessageMapper {

    public abstract FirstPartyRetailerApplicationOutboundResult toResult(MerchantAccountPort.FirstPartyApplicationResult result);
}

