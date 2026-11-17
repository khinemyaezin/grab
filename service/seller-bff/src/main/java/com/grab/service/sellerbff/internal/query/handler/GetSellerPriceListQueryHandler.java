package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerPricingQueries.GetSellerPriceListQuery;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PriceListOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerPriceListQueryHandler
        implements QueryHandler<GetSellerPriceListQuery, Optional<PriceListOutboundResult>> {

    private final SellerPricingOutboundPort outboundPort;

    public GetSellerPriceListQueryHandler(SellerPricingOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<PriceListOutboundResult> handle(GetSellerPriceListQuery query) {
        return outboundPort.findPriceListById(query.priceListId());
    }

    @Override
    public Class<GetSellerPriceListQuery> getQueryType() {
        return GetSellerPriceListQuery.class;
    }
}

