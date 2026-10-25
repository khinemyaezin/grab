package com.grab.app.channeldiscovery.storefrontquery.internal.api.rest.service;

import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;
import com.grab.discovery.application.model.read.ResolveShoppingContextQuery;
import com.grab.discovery.application.model.read.ResolveShoppingContextResult;
import com.grab.discovery.application.model.read.SearchBuyableOffersQuery;
import com.grab.discovery.application.model.read.ShoppingContext;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.app.channeldiscovery.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
import com.grab.app.channeldiscovery.storefrontquery.internal.api.rest.dto.response.ShoppingContextResponse;
import com.grab.app.channeldiscovery.storefrontquery.internal.api.rest.mapper.GetBuyableOfferBySlugRequestMapper;
import com.grab.app.channeldiscovery.storefrontquery.internal.api.rest.mapper.ResolveShoppingContextRequestMapper;
import com.grab.app.channeldiscovery.storefrontquery.internal.api.rest.mapper.SearchBuyableOffersRequestMapper;
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
            String publishableKey,
            String countryCode,
            String locale,
            String keyword,
            String categoryId,
            Pageable pageable
    ) {
        ResolveShoppingContextResult contextResult = dispatchContext(publishableKey, countryCode, locale);
        ShoppingContext context = contextResult.context();
        SearchBuyableOffersQuery query = searchMapper.toQuery(
                context,
                contextResult.regionServed(),
                keyword,
                categoryId,
                pageable
        );
        Page<BuyableOfferResult> results = queryBus.dispatch(query);
        return results.map(searchMapper::toResponse);
    }

    public BuyableOfferResponse getBySlug(
            String publishableKey,
            String countryCode,
            String locale,
            String slug
    ) {
        ResolveShoppingContextResult contextResult = dispatchContext(publishableKey, countryCode, locale);
        GetBuyableOfferBySlugQuery query = getMapper.toQuery(
                contextResult.context(),
                contextResult.regionServed(),
                slug
        );
        BuyableOfferResult result = queryBus.dispatch(query);
        return getMapper.toResponse(result);
    }

    public ShoppingContextResponse resolveShoppingContext(
            String publishableKey,
            String countryCode,
            String locale
    ) {
        ResolveShoppingContextResult result = dispatchContext(publishableKey, countryCode, locale);
        return resolveContextMapper.toResponse(result);
    }

    private ResolveShoppingContextResult dispatchContext(
            String publishableKey,
            String countryCode,
            String locale
    ) {
        ResolveShoppingContextQuery query = resolveContextMapper.toQuery(publishableKey, countryCode, locale);
        return queryBus.dispatch(query);
    }
}
