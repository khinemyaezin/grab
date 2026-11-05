package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryFacilityPort;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.CreateZoneRecord;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityZoneOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
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
