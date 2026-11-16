package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.account.merchant.port.message.MerchantAccountPort;
import com.grab.app.platform.seller.port.outbound.SellerMerchantOutboundPort.C2CApplicationOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetC2CApplicationMessageMapper {

    public abstract C2CApplicationOutboundResult toResult(MerchantAccountPort.C2CApplicationResult result);
}

