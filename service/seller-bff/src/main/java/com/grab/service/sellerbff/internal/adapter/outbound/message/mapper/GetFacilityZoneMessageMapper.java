package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.inventory.InventoryFacilityPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.FacilityZoneOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetFacilityZoneMessageMapper {

    public abstract FacilityZoneOutboundResult toResult(InventoryFacilityPort.FacilityZoneResult result);
}

