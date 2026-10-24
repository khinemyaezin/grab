package com.catalog.application.model.read;

import com.grab.framework.id.Id;

public record ListPublishedProductChannelsForSalesChannelAfterQuery(
        Id salesChannelId,
        Id afterProductId,
        int limit
) {
}
