package com.grab.store.storefrontquery.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.saleschannel.query.SalesChannelQueryPort;
import com.grab.store.storefrontquery.internal.config.StorefrontQueryTransactional;
import com.grab.store.storefrontquery.internal.exception.StorefrontQueryServiceError;
import com.grab.store.storefrontquery.internal.exception.StorefrontQueryServiceException;
import com.grab.store.storefrontquery.internal.query.BuyableOfferResult;
import com.grab.store.storefrontquery.internal.query.SearchBuyableOffersQuery;
import com.storefrontquery.infrastructure.entity.BuyableOfferEntity;
import com.storefrontquery.infrastructure.repository.jpa.BuyableOfferJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchBuyableOffersQueryHandler
        implements QueryHandler<SearchBuyableOffersQuery, Page<BuyableOfferResult>> {
    private final BuyableOfferJpaRepository offers;
    private final SalesChannelQueryPort salesChannels;

    @Override
    @StorefrontQueryTransactional(readOnly = true)
    public Page<BuyableOfferResult> handle(SearchBuyableOffersQuery query) {
        requireEnabledChannel(query.salesChannelId());
        Pageable pageable = query.pageable() == null ? Pageable.unpaged() : query.pageable();
        return offers.findBySalesChannelIdAndBuyableTrue(query.salesChannelId(), pageable)
                .map(this::toResult);
    }

    @Override
    public Class<SearchBuyableOffersQuery> getQueryType() {
        return SearchBuyableOffersQuery.class;
    }

    private void requireEnabledChannel(String salesChannelId) {
        if (salesChannelId == null || salesChannelId.isBlank()) {
            throw new StorefrontQueryServiceException(
                    new StorefrontQueryServiceError.SalesChannelRequired(),
                    "salesChannelId is required"
            );
        }
        if (!salesChannels.isEnabled(salesChannelId)) {
            throw new StorefrontQueryServiceException(
                    new StorefrontQueryServiceError.ChannelDisabled(salesChannelId),
                    "Sales channel is disabled"
            );
        }
    }

    private BuyableOfferResult toResult(BuyableOfferEntity entity) {
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
}
