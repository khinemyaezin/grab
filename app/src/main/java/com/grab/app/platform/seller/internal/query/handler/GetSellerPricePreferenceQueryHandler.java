package com.grab.app.platform.seller.internal.query.handler;

import com.grab.app.platform.seller.internal.query.model.SellerPricingQueries.GetSellerPricePreferenceQuery;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort;
import com.grab.app.platform.seller.port.outbound.SellerPricingOutboundPort.PricePreferenceOutboundResult;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetSellerPricePreferenceQueryHandler
        implements QueryHandler<GetSellerPricePreferenceQuery, Optional<PricePreferenceOutboundResult>> {

    private final SellerPricingOutboundPort outboundPort;

    public GetSellerPricePreferenceQueryHandler(SellerPricingOutboundPort outboundPort) {
        this.outboundPort = outboundPort;
    }

    @Override
    public Optional<PricePreferenceOutboundResult> handle(GetSellerPricePreferenceQuery query) {
        return outboundPort.findPricePreference(query.preferenceId());
    }

    @Override
    public Class<GetSellerPricePreferenceQuery> getQueryType() {
        return GetSellerPricePreferenceQuery.class;
    }
}

