package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryFacilityPort;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.CreateBinRecord;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityBinOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
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
