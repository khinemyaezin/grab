package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.inventory.InventoryAvailabilityPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.AllocateStockRecord;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.AllocationOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class AllocateStockMessageMapper {

    @Mapping(target = "sku", source = "record.sku")
    @Mapping(target = "quantity", source = "record.quantity")
    @Mapping(target = "orderId", source = "record.orderId")
    @Mapping(target = "locationId", source = "record.locationId")
    @Mapping(target = "createdBy", source = "record.actorId")
    @Mapping(target = "orderLineId", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "scopeKey", ignore = true)
    @Mapping(target = "scopeId", ignore = true)
    public abstract InventoryAvailabilityPort.AllocateStockMessage toMessage(AllocateStockRecord record);

    public abstract AllocationOutboundResult toResult(InventoryAvailabilityPort.AllocationMessageResult result);
}
