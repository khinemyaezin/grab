package com.catalog.application.port.outbound;

import com.catalog.application.model.read.PublishedProductChannel;
import com.grab.framework.id.Id;

import java.util.List;

public interface PublishedProductChannelQueryPort {
    List<PublishedProductChannel> findAfter(Id afterProductId, Id afterSalesChannelId, int limit);

    List<PublishedProductChannel> findBySku(String sku);

    List<PublishedProductChannel> findBySalesChannelAfter(
            Id salesChannelId,
            Id afterProductId,
            int limit
    );
}
