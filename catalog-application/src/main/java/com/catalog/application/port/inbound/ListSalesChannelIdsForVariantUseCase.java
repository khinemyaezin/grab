package com.catalog.application.port.inbound;

import com.catalog.application.model.read.ListSalesChannelIdsForVariantQuery;

import java.util.List;

public interface ListSalesChannelIdsForVariantUseCase {
    List<String> execute(ListSalesChannelIdsForVariantQuery query);
}
