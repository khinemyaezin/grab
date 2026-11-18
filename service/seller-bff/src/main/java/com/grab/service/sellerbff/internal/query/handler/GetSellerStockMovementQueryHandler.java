package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerInventoryQueries.GetSellerStockMovementQuery;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.StockMovementOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerStockMovementQueryHandler
        implements QueryHandler<GetSellerStockMovementQuery, Optional<StockMovementOutboundResult>> {

    private final SellerInventoryOutboundPort outboundPort;

    public GetSellerStockMovementQueryHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<StockMovementOutboundResult> handle(GetSellerStockMovementQuery query) {
        return outboundPort.findLatestMovement(query.itemId());
    }

    @Override
    public Class<GetSellerStockMovementQuery> getQueryType() {
        return GetSellerStockMovementQuery.class;
    }
}

