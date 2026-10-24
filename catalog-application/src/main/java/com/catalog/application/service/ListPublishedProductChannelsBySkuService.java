package com.catalog.application.service;

import com.catalog.application.model.read.ListPublishedProductChannelsBySkuQuery;
import com.catalog.application.model.read.PublishedProductChannel;
import com.catalog.application.port.inbound.ListPublishedProductChannelsBySkuUseCase;
import com.catalog.application.port.outbound.PublishedProductChannelQueryPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ListPublishedProductChannelsBySkuService implements ListPublishedProductChannelsBySkuUseCase {

    private final PublishedProductChannelQueryPort queryPort;

    @Override
    public List<PublishedProductChannel> execute(ListPublishedProductChannelsBySkuQuery query) {
        if (query.sku() == null || query.sku().isBlank()) {
            throw new IllegalArgumentException("sku must not be blank");
        }
        return queryPort.findBySku(query.sku());
    }
}
