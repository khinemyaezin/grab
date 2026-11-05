package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerPricingQueries.ListSellerPriceListsQuery;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceListOutboundResult;
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

