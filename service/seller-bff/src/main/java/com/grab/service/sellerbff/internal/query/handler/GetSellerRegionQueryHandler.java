package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerRegionQueries.GetSellerRegionQuery;
import com.grab.service.sellerbff.port.outbound.SellerRegionOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerRegionOutboundPort.RegionOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerRegionQueryHandler
        implements QueryHandler<GetSellerRegionQuery, Optional<RegionOutboundResult>> {

    private final SellerRegionOutboundPort outboundPort;

    public GetSellerRegionQueryHandler(SellerRegionOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<RegionOutboundResult> handle(GetSellerRegionQuery query) {
        return outboundPort.findRegionById(query.regionId());
    }

    @Override
    public Class<GetSellerRegionQuery> getQueryType() {
        return GetSellerRegionQuery.class;
    }
}

