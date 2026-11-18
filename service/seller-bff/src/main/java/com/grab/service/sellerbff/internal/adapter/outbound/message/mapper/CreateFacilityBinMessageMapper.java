package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.inventory.InventoryFacilityPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.CreateBinRecord;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.FacilityBinOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateFacilityBinMessageMapper {

    @Mapping(target = "code", source = "record.code")
    @Mapping(target = "name", source = "record.name")
    @Mapping(target = "maxCapacity", source = "record.maxCapacity")
    @Mapping(target = "actorId", source = "record.actorId")
    @Mapping(target = "scopeKey", ignore = true)
    @Mapping(target = "scopeId", ignore = true)
    public abstract InventoryFacilityPort.CreateBinMessage toMessage(CreateBinRecord record);

    public abstract FacilityBinOutboundResult toResult(InventoryFacilityPort.FacilityBinResult result);
}
