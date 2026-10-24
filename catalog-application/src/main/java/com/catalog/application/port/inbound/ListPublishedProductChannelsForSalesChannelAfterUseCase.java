package com.catalog.application.port.inbound;

import com.catalog.application.model.read.ListPublishedProductChannelsForSalesChannelAfterQuery;
import com.catalog.application.model.read.PublishedProductChannel;

import java.util.List;

public interface ListPublishedProductChannelsForSalesChannelAfterUseCase {
    List<PublishedProductChannel> execute(ListPublishedProductChannelsForSalesChannelAfterQuery query);
}
