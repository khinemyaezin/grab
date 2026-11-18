package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerInventoryQueries.GetSellerZoneQuery;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerInventoryOutboundPort.FacilityZoneOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerZoneQueryHandler
        implements QueryHandler<GetSellerZoneQuery, Optional<FacilityZoneOutboundResult>> {

    private final SellerInventoryOutboundPort outboundPort;

    public GetSellerZoneQueryHandler(SellerInventoryOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<FacilityZoneOutboundResult> handle(GetSellerZoneQuery query) {
        return outboundPort.findZoneById(query.zoneId());
    }

    @Override
    public Class<GetSellerZoneQuery> getQueryType() {
        return GetSellerZoneQuery.class;
    }
}

