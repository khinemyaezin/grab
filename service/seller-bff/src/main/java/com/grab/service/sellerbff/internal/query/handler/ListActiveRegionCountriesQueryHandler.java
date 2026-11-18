package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerRegionQueries.ListActiveRegionCountriesQuery;
import com.grab.service.sellerbff.port.outbound.SellerRegionOutboundPort;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListActiveRegionCountriesQueryHandler
        implements QueryHandler<ListActiveRegionCountriesQuery, List<String>> {

    private final SellerRegionOutboundPort outboundPort;

    public ListActiveRegionCountriesQueryHandler(SellerRegionOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<String> handle(ListActiveRegionCountriesQuery query) {
        return outboundPort.listSupportedCountryCodes();
    }

    @Override
    public Class<ListActiveRegionCountriesQuery> getQueryType() {
        return ListActiveRegionCountriesQuery.class;
    }
}

