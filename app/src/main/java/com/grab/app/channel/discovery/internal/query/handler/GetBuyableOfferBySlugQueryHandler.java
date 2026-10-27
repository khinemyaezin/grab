package com.grab.app.channel.discovery.internal.query.handler;

import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;
import com.grab.discovery.application.port.inbound.GetBuyableOfferBySlugUseCase;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.app.channel.discovery.internal.config.StorefrontQueryReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetBuyableOfferBySlugQueryHandler
        implements QueryHandler<GetBuyableOfferBySlugQuery, BuyableOfferResult> {
    private final GetBuyableOfferBySlugUseCase getBuyableOfferBySlugUseCase;

    @Override
    @StorefrontQueryReadTransactional
    public BuyableOfferResult handle(GetBuyableOfferBySlugQuery query) {
        return getBuyableOfferBySlugUseCase.execute(query);
    }

    @Override
    public Class<GetBuyableOfferBySlugQuery> getQueryType() {
        return GetBuyableOfferBySlugQuery.class;
    }
}

