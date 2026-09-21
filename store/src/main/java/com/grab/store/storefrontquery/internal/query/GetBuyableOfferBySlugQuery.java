package com.grab.store.storefrontquery.internal.query;

import com.grab.framework.cqrs.query.Query;

public record GetBuyableOfferBySlugQuery(
        String salesChannelId,
        String slug
) implements Query<BuyableOfferResult> {
}
