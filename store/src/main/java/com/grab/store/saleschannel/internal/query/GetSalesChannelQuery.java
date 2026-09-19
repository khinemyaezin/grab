package com.grab.store.saleschannel.internal.query;

import com.grab.framework.cqrs.query.Query;

public record GetSalesChannelQuery(
        String salesChannelId,
        String merchantId
) implements Query<SalesChannelResult> {
}
