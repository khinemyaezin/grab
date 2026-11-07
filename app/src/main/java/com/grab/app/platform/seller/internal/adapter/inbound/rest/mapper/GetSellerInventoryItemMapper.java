package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.InventoryItemResponse;
import com.grab.app.platform.seller.internal.query.model.SellerInventoryQueries.GetSellerInventoryItemQuery;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.InventoryItemOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerInventoryItemMapper {

    public abstract GetSellerInventoryItemQuery toQuery(String itemId);

    public abstract InventoryItemResponse toResponse(InventoryItemOutboundResult result);
}

