package com.catalog.application.port.inbound;

import com.catalog.application.model.read.ListPublishedProductChannelsBySkuQuery;
import com.catalog.application.model.read.PublishedProductChannel;

import java.util.List;

public interface ListPublishedProductChannelsBySkuUseCase {
    List<PublishedProductChannel> execute(ListPublishedProductChannelsBySkuQuery query);
}
