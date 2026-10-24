package com.catalog.application.port.inbound;

import com.catalog.application.model.read.ListPublishedProductChannelsAfterQuery;
import com.catalog.application.model.read.ListPublishedProductChannelsAfterResult;

public interface ListPublishedProductChannelsAfterUseCase {
    ListPublishedProductChannelsAfterResult execute(ListPublishedProductChannelsAfterQuery query);
}
