package com.grab.service.sellerbff.internal.adapter.outbound.message.mapper;

import com.grab.service.contract.message.merchandising.inventory.InventoryFacilityPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.CreateZoneRecord;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.FacilityZoneOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateFacilityZoneMessageMapper {

    @Mapping(target = "code", source = "record.code")
    @Mapping(target = "name", source = "record.name")
    @Mapping(target = "type", source = "record.type")
    @Mapping(target = "actorId", source = "record.actorId")
    @Mapping(target = "scopeKey", ignore = true)
    @Mapping(target = "scopeId", ignore = true)
    public abstract InventoryFacilityPort.CreateZoneMessage toMessage(CreateZoneRecord record);

    public abstract FacilityZoneOutboundResult toResult(InventoryFacilityPort.FacilityZoneResult result);
}
