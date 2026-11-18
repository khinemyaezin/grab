package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerPricingQueries.ListSellerPriceListsQuery;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PriceListOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListSellerPriceListsQueryHandler
        implements QueryHandler<ListSellerPriceListsQuery, List<PriceListOutboundResult>> {

    private final SellerPricingOutboundPort outboundPort;

    public ListSellerPriceListsQueryHandler(SellerPricingOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public List<PriceListOutboundResult> handle(ListSellerPriceListsQuery query) {
        return outboundPort.listPriceLists();
    }

    @Override
    public Class<ListSellerPriceListsQuery> getQueryType() {
        return ListSellerPriceListsQuery.class;
    }
}

