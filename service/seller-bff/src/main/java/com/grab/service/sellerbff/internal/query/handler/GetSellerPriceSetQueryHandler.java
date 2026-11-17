package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerPricingQueries.GetSellerPriceSetQuery;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PriceSetOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerPriceSetQueryHandler
        implements QueryHandler<GetSellerPriceSetQuery, Optional<PriceSetOutboundResult>> {

    private final SellerPricingOutboundPort outboundPort;

    public GetSellerPriceSetQueryHandler(SellerPricingOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<PriceSetOutboundResult> handle(GetSellerPriceSetQuery query) {
        return outboundPort.findPriceSetById(query.priceSetId());
    }

    @Override
    public Class<GetSellerPriceSetQuery> getQueryType() {
        return GetSellerPriceSetQuery.class;
    }
}

