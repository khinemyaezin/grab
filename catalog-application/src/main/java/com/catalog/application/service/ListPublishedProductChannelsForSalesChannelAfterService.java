package com.catalog.application.service;

import com.catalog.application.model.read.ListPublishedProductChannelsForSalesChannelAfterQuery;
import com.catalog.application.model.read.PublishedProductChannel;
import com.catalog.application.port.inbound.ListPublishedProductChannelsForSalesChannelAfterUseCase;
import com.catalog.application.port.outbound.PublishedProductChannelQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListPublishedProductChannelsForSalesChannelAfterService
        implements ListPublishedProductChannelsForSalesChannelAfterUseCase {

    private final PublishedProductChannelQueryPort queryPort;

    @Override
    public List<PublishedProductChannel> execute(ListPublishedProductChannelsForSalesChannelAfterQuery query) {
        if (query.salesChannelId() == null) {
            throw new IllegalArgumentException("salesChannelId must not be null");
        }
        if (query.limit() < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
        if (query.afterProductId() == null) {
            return queryPort.findBySalesChannelAfter(query.salesChannelId(), null, query.limit());
        }
        return queryPort.findBySalesChannelAfter(
                query.salesChannelId(),
                query.afterProductId(),
                query.limit()
        );
    }
}
