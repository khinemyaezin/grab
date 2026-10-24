package com.catalog.application.model.read;

import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;

public record ListPublishedProductChannelsAfterQuery(Id afterProductId, Id afterSalesChannelId, int limit)
        implements Query<ListPublishedProductChannelsAfterResult> {
}
