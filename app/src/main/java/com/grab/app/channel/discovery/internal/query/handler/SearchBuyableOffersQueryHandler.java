package com.grab.app.channel.discovery.internal.query.handler;

import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.discovery.application.model.read.SearchBuyableOffersQuery;
import com.grab.discovery.application.port.inbound.SearchBuyableOffersUseCase;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.app.channel.discovery.internal.config.StorefrontQueryReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchBuyableOffersQueryHandler
        implements QueryHandler<SearchBuyableOffersQuery, Page<BuyableOfferResult>> {
    private final SearchBuyableOffersUseCase searchBuyableOffersUseCase;

    @Override
    @StorefrontQueryReadTransactional
    public Page<BuyableOfferResult> handle(SearchBuyableOffersQuery query) {
        return searchBuyableOffersUseCase.execute(query);
    }

    @Override
    public Class<SearchBuyableOffersQuery> getQueryType() {
        return SearchBuyableOffersQuery.class;
    }
}

