package com.grab.store.storefrontquery.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.storefrontquery.internal.api.rest.dto.response.BuyableOfferResponse;
import com.grab.store.storefrontquery.internal.query.BuyableOfferResult;
import com.grab.store.storefrontquery.internal.query.GetBuyableOfferBySlugQuery;
import com.grab.store.storefrontquery.internal.query.SearchBuyableOffersQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorefrontQueryService {
    private final QueryBus queryBus;

    public Page<BuyableOfferResponse> list(String salesChannelId, Pageable pageable) {
        return queryBus.dispatch(new SearchBuyableOffersQuery(salesChannelId, pageable)).map(this::toResponse);
    }

    public BuyableOfferResponse getBySlug(String salesChannelId, String slug) {
        return toResponse(queryBus.dispatch(new GetBuyableOfferBySlugQuery(salesChannelId, slug)));
    }

    private BuyableOfferResponse toResponse(BuyableOfferResult result) {
        return new BuyableOfferResponse(
                result.salesChannelId(),
                result.variantId(),
                result.productId(),
                result.sellerId(),
                result.sku(),
                result.title(),
                result.slug(),
                result.media(),
                result.productStatus(),
                result.amount(),
                result.currency(),
                result.availableQty(),
                result.untracked(),
                result.buyable()
        );
    }
}
