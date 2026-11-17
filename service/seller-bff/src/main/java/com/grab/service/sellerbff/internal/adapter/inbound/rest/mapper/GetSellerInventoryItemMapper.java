package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.InventoryItemResponse;
import com.grab.service.sellerbff.internal.query.model.SellerInventoryQueries.GetSellerInventoryItemQuery;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.InventoryItemOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerInventoryItemMapper {

    public abstract GetSellerInventoryItemQuery toQuery(String itemId);

    public abstract InventoryItemResponse toResponse(InventoryItemOutboundResult result);
}

