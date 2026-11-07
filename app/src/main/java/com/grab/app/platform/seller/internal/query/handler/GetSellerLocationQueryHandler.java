package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerInventoryQueries.GetSellerLocationQuery;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerInventoryOutboundPort.FacilityLocationOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerLocationQueryHandler
        implements QueryHandler<GetSellerLocationQuery, Optional<FacilityLocationOutboundResult>> {

    private final SellerInventoryOutboundPort outboundPort;

    public GetSellerLocationQueryHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<FacilityLocationOutboundResult> handle(GetSellerLocationQuery query) {
        return outboundPort.findLocationById(query.locationId());
    }

    @Override
    public Class<GetSellerLocationQuery> getQueryType() {
        return GetSellerLocationQuery.class;
    }
}

