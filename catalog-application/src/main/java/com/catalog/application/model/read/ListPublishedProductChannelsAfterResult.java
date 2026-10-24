package com.catalog.application.model.read;

import java.util.List;

public record ListPublishedProductChannelsAfterResult(List<PublishedProductChannel> items) {
    public ListPublishedProductChannelsAfterResult {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
