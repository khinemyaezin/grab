package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerPricingQueries.GetSellerPriceSetQuery;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PriceSetOutboundResult;
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

