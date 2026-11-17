package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerInventoryQueries.GetSellerBinQuery;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.FacilityBinOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerBinQueryHandler
        implements QueryHandler<GetSellerBinQuery, Optional<FacilityBinOutboundResult>> {

    private final SellerInventoryOutboundPort outboundPort;

    public GetSellerBinQueryHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<FacilityBinOutboundResult> handle(GetSellerBinQuery query) {
        return outboundPort.findBinById(query.binId());
    }

    @Override
    public Class<GetSellerBinQuery> getQueryType() {
        return GetSellerBinQuery.class;
    }
}

