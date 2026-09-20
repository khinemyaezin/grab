package com.saleschannel.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record CheckSalesChannelUsableQuery(
        String salesChannelId,
        String merchantId
) implements Query<CheckSalesChannelUsableResult> {
}
