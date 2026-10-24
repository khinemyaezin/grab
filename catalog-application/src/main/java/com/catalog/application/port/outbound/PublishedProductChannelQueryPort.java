package com.catalog.application.port.outbound;

import com.catalog.application.model.read.PublishedProductChannel;
import com.grab.framework.id.Id;

import java.util.List;

public interface PublishedProductChannelQueryPort {
    List<PublishedProductChannel> findAfter(Id afterProductId, Id afterSalesChannelId, int limit);
}
