package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerInventoryQueries.GetSellerInventoryItemQuery;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.InventoryItemOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerInventoryItemQueryHandler
        implements QueryHandler<GetSellerInventoryItemQuery, Optional<InventoryItemOutboundResult>> {

    private final SellerInventoryOutboundPort outboundPort;

    public GetSellerInventoryItemQueryHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<InventoryItemOutboundResult> handle(GetSellerInventoryItemQuery query) {
        return outboundPort.findItemById(query.itemId());
    }

    @Override
    public Class<GetSellerInventoryItemQuery> getQueryType() {
        return GetSellerInventoryItemQuery.class;
    }
}

