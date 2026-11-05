package com.grab.app.platform.seller.internal.adapter.outbound.message.mapper;

import com.grab.app.merchandising.inventory.port.message.InventoryItemPort;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.CreateItemRecord;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.InventoryItemOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import com.grab.framework.mapper.IdMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class, uses = IdMapper.class)
public abstract class CreateInventoryItemMessageMapper {

    @Mapping(target = "merchantId", source = "record.merchantId")
    @Mapping(target = "createdBy", source = "record.actorId")
    @Mapping(target = "locationId", source = "record.locationId")
    @Mapping(target = "sku", source = "record.sku")
    @Mapping(target = "initialQuantity", source = "record.initialQuantity")
    @Mapping(target = "safetyStock", source = "record.safetyStock")
    @Mapping(target = "reorderPoint", source = "record.reorderPoint")
    @Mapping(target = "reorderQuantity", source = "record.reorderQuantity")
    @Mapping(target = "maxStock", source = "record.maxStock")
    @Mapping(target = "variantId", ignore = true)
    @Mapping(target = "scopeKey", ignore = true)
    @Mapping(target = "scopeId", ignore = true)
    public abstract InventoryItemPort.CreateInventoryItemMessage toMessage(CreateItemRecord record);

    public abstract InventoryItemOutboundResult toResult(InventoryItemPort.ItemMessageResult result);
}
