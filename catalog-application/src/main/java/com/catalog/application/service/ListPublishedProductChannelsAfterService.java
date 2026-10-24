package com.catalog.application.service;

import com.catalog.application.model.read.ListPublishedProductChannelsAfterQuery;
import com.catalog.application.model.read.ListPublishedProductChannelsAfterResult;
import com.catalog.application.model.read.PublishedProductChannel;
import com.catalog.application.port.inbound.ListPublishedProductChannelsAfterUseCase;
import com.catalog.application.port.outbound.PublishedProductChannelQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListPublishedProductChannelsAfterService implements ListPublishedProductChannelsAfterUseCase {

    private final PublishedProductChannelQueryPort queryPort;

    @Override
    public ListPublishedProductChannelsAfterResult execute(ListPublishedProductChannelsAfterQuery query) {
        if (query.limit() < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
        boolean missingProductCursor = query.afterProductId() == null;
        boolean missingChannelCursor = query.afterSalesChannelId() == null;
        if (missingProductCursor != missingChannelCursor) {
            throw new IllegalArgumentException("Both publication cursors must be present together.");
        }
        List<PublishedProductChannel> results = queryPort.findAfter(
                query.afterProductId(),
                query.afterSalesChannelId(),
                query.limit()
        );
        return new ListPublishedProductChannelsAfterResult(results);
    }
}
