package com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.StockMovementResponse;
import com.grab.app.platform.seller.internal.query.model.SellerInventoryQueries.GetSellerStockMovementQuery;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.StockMovementOutboundResult;
import com.grab.app.shared.config.CentralMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public abstract class GetSellerStockMovementMapper {

    public abstract GetSellerStockMovementQuery toQuery(String itemId);

    public abstract StockMovementResponse toResponse(StockMovementOutboundResult result);
}

