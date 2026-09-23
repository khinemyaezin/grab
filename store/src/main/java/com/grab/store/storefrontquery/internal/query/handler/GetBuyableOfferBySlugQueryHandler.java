package com.grab.store.storefrontquery.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.saleschannel.port.SalesChannelQuery;
import com.grab.store.storefrontquery.internal.config.StorefrontQueryTransactional;
import com.grab.store.storefrontquery.internal.exception.StorefrontQueryServiceError;
import com.grab.store.storefrontquery.internal.exception.StorefrontQueryServiceException;
import com.grab.store.storefrontquery.internal.query.BuyableOfferResult;
import com.grab.store.storefrontquery.internal.query.GetBuyableOfferBySlugQuery;
import com.storefrontquery.infrastructure.entity.BuyableOfferEntity;
import com.storefrontquery.infrastructure.repository.jpa.BuyableOfferJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetBuyableOfferBySlugQueryHandler
        implements QueryHandler<GetBuyableOfferBySlugQuery, BuyableOfferResult> {
    private final BuyableOfferJpaRepository offers;
    private final SalesChannelQuery salesChannels;

    @Override
    @StorefrontQueryTransactional(readOnly = true)
    public BuyableOfferResult handle(GetBuyableOfferBySlugQuery query) {
        if (query.salesChannelId() == null || query.salesChannelId().isBlank()) {
            throw new StorefrontQueryServiceException(
                    new StorefrontQueryServiceError.SalesChannelRequired(),
                    "salesChannelId is required"
            );
        }
        if (!salesChannels.isEnabled(query.salesChannelId())) {
            throw new StorefrontQueryServiceException(
                    new StorefrontQueryServiceError.ChannelDisabled(query.salesChannelId()),
                    "Sales channel is disabled"
            );
        }
        BuyableOfferEntity entity = offers
                .findBySalesChannelIdAndSlugAndBuyableTrue(query.salesChannelId(), query.slug())
                .orElseThrow(() -> new StorefrontQueryServiceException(
                        new StorefrontQueryServiceError.OfferNotFound(query.slug(), query.salesChannelId()),
                        "Buyable offer not found"
                ));
        return new BuyableOfferResult(
                entity.getSalesChannelId(),
                entity.getVariantId(),
                entity.getProductId(),
                entity.getSellerId(),
                entity.getSku(),
                entity.getTitle(),
                entity.getSlug(),
                entity.getMedia(),
                entity.getProductStatus(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getAvailableQty(),
                entity.isUntracked(),
                entity.isBuyable()
        );
    }

    @Override
    public Class<GetBuyableOfferBySlugQuery> getQueryType() {
        return GetBuyableOfferBySlugQuery.class;
    }
}
