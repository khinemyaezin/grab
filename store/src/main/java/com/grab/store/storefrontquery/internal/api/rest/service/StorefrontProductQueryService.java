package com.grab.store.storefrontquery.internal.api.rest.service;

import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;
import com.grab.discovery.application.model.read.SearchBuyableOffersQuery;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
import com.grab.store.storefrontquery.internal.api.rest.mapper.GetBuyableOfferBySlugRequestMapper;
import com.grab.store.storefrontquery.internal.api.rest.mapper.SearchBuyableOffersRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorefrontProductQueryService {
    private final QueryBus queryBus;
    private final SearchBuyableOffersRequestMapper searchMapper;
    private final GetBuyableOfferBySlugRequestMapper getMapper;

    public Page<BuyableOfferResponse> list(String salesChannelId, Pageable pageable) {
        SearchBuyableOffersQuery query = searchMapper.toQuery(salesChannelId, pageable);
        Page<DiscoveryProductView> results = queryBus.dispatch(query);
        return results.map(searchMapper::toResponse);
    }

    public BuyableOfferResponse getBySlug(String salesChannelId, String slug) {
        GetBuyableOfferBySlugQuery query = getMapper.toQuery(salesChannelId, slug);
        DiscoveryProductView result = queryBus.dispatch(query);
        return getMapper.toResponse(result);
    }
}
