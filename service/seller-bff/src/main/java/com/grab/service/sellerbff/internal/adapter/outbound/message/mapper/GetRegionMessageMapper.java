package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.channel.region.RegionPort.RegionMessageResult;
import com.grab.service.sellerbff.port.outbound.SellerRegionOutboundPort.RegionOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetRegionMessageMapper {

    public abstract RegionOutboundResult toResult(RegionMessageResult result);
}

