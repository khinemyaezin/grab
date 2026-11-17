package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerInventoryQueries.GetSellerLocationQuery;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.FacilityLocationOutboundResult;
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

