package com.saleschannel.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record GetSalesChannelQuery(
        String salesChannelId,
        String merchantId
) implements Query<SalesChannelResult> {
}
