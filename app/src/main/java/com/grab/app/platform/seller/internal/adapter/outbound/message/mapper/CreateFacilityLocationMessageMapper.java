package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryFacilityPort;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.CreateLocationRecord;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityLocationOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateFacilityLocationMessageMapper {

    @Mapping(target = "merchantId", source = "record.merchantId")
    @Mapping(target = "code", source = "record.code")
    @Mapping(target = "name", source = "record.name")
    @Mapping(target = "type", source = "record.type")
    @Mapping(target = "line1", source = "record.line1")
    @Mapping(target = "line2", source = "record.line2")
    @Mapping(target = "city", source = "record.city")
    @Mapping(target = "state", source = "record.state")
    @Mapping(target = "postalCode", source = "record.postalCode")
    @Mapping(target = "country", source = "record.country")
    @Mapping(target = "createdBy", source = "record.actorId")
    public abstract InventoryFacilityPort.CreateLocationMessage toMessage(CreateLocationRecord record);

    public abstract FacilityLocationOutboundResult toResult(InventoryFacilityPort.FacilityLocationResult result);
}
