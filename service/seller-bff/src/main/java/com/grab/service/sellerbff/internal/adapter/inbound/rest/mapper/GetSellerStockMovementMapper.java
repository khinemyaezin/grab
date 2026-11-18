package com.grab.service.sellerbff.internal.adapter.inbound.rest.mapper;

import com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response.StockMovementResponse;
import com.grab.service.sellerbff.internal.query.model.SellerInventoryQueries.GetSellerStockMovementQuery;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.StockMovementOutboundResult;
import com.grab.service.sellerbff.internal.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerStockMovementMapper {

    public abstract GetSellerStockMovementQuery toQuery(String itemId);

    public abstract StockMovementResponse toResponse(StockMovementOutboundResult result);
}

