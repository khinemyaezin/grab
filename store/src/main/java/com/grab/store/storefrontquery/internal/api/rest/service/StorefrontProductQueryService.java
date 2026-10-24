package com.grab.store.storefrontquery.internal.api.rest.service;

import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;
import com.grab.discovery.application.model.read.ResolveShoppingContextQuery;
import com.grab.discovery.application.model.read.ResolveShoppingContextResult;
import com.grab.discovery.application.model.read.SearchBuyableOffersQuery;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.ShoppingContextResponse;
import com.grab.store.storefrontquery.internal.api.rest.mapper.GetBuyableOfferBySlugRequestMapper;
import com.grab.store.storefrontquery.internal.api.rest.mapper.ResolveShoppingContextRequestMapper;
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
    private final ResolveShoppingContextRequestMapper resolveContextMapper;

    public Page<BuyableOfferResponse> list(
            String salesChannelId,
            String keyword,
            String categoryId,
            Pageable pageable
    ) {
        SearchBuyableOffersQuery query = searchMapper.toQuery(salesChannelId, keyword, categoryId, pageable);
        Page<DiscoveryProductView> results = queryBus.dispatch(query);
        return results.map(searchMapper::toResponse);
    }

    public BuyableOfferResponse getBySlug(String salesChannelId, String slug) {
        GetBuyableOfferBySlugQuery query = getMapper.toQuery(salesChannelId, slug);
        DiscoveryProductView result = queryBus.dispatch(query);
        return getMapper.toResponse(result);
    }

    public ShoppingContextResponse resolveShoppingContext(
            String publishableKey,
            String countryCode,
            String locale
    ) {
        ResolveShoppingContextQuery query = resolveContextMapper.toQuery(publishableKey, countryCode, locale);
        ResolveShoppingContextResult result = queryBus.dispatch(query);
        return resolveContextMapper.toResponse(result);
    }
}
