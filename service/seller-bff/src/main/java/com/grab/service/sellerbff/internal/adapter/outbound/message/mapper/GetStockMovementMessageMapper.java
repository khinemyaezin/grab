package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.inventory.InventoryItemPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.StockMovementOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class GetStockMovementMessageMapper {

    public abstract StockMovementOutboundResult toResult(InventoryItemPort.MovementMessageResult result);
}
