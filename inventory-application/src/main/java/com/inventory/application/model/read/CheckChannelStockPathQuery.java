package com.inventory.application.model.read;

import com.grab.framework.cqrs.query.Query;

public record CheckChannelStockPathQuery(
        String merchantId,
        String salesChannelId
) implements Query<CheckChannelStockPathResult> {
}
