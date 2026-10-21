package com.grab.discovery.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

public record GetBuyableOfferBySlugQuery(
        Id salesChannelId,
        String slug
) implements Query<DiscoveryProductView> {
}
