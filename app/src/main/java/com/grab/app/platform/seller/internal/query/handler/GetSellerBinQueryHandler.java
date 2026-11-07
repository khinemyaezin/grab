package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerInventoryQueries.GetSellerBinQuery;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityBinOutboundResult;
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

