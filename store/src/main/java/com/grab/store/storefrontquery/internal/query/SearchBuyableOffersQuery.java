package com.grab.store.storefrontquery.internal.query;

import com.grab.framework.cqrs.query.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record SearchBuyableOffersQuery(
        String salesChannelId,
        Pageable pageable
) implements Query<Page<BuyableOfferResult>> {
}
