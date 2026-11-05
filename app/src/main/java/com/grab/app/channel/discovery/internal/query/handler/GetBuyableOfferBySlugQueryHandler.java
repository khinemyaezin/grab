package com.grab.app.channel.discovery.internal.query.handler;

import com.grab.app.channel.discovery.internal.config.DiscoveryReadTransactional;
import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;
import com.grab.discovery.application.port.inbound.GetBuyableOfferBySlugUseCase;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetBuyableOfferBySlugQueryHandler
        implements QueryHandler<GetBuyableOfferBySlugQuery, BuyableOfferResult> {

    private final GetBuyableOfferBySlugUseCase getBuyableOfferBySlugUseCase;

    public GetBuyableOfferBySlugQueryHandler(GetBuyableOfferBySlugUseCase getBuyableOfferBySlugUseCase) {
        this.getBuyableOfferBySlugUseCase = getBuyableOfferBySlugUseCase;
    }

    @Override
    @DiscoveryReadTransactional
    public BuyableOfferResult handle(GetBuyableOfferBySlugQuery query) {
        return getBuyableOfferBySlugUseCase.execute(query);
    }

    @Override
    public Class<GetBuyableOfferBySlugQuery> getQueryType() {
        return GetBuyableOfferBySlugQuery.class;
    }
}

