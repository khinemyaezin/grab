package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.inventory.InventoryFacilityPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.FacilityLocationOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetFacilityLocationMessageMapper {

    public abstract FacilityLocationOutboundResult toResult(InventoryFacilityPort.FacilityLocationResult result);
}

