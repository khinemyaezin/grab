package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.inventory.InventoryItemPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.InventoryItemOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetInventoryItemMessageMapper {

    public abstract InventoryItemOutboundResult toResult(InventoryItemPort.ItemMessageResult result);
}

