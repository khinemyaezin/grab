package com.grab.service.sellerbff.internal.query.handler;

import com.grab.service.sellerbff.internal.query.model.SellerPricingQueries.GetSellerPricePreferenceQuery;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort;
import com.grab.service.sellerbff.port.outbound.SellerPricingOutboundPort.PricePreferenceOutboundResult;
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

