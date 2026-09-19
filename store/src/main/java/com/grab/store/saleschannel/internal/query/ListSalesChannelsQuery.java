package com.grab.store.saleschannel.internal.query;

import com.grab.framework.cqrs.query.Query;
import com.grab.store.shared.PageableQueryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record ListSalesChannelsQuery(
        String merchantId,
        Pageable pageable
) implements Query<Page<SalesChannelResult>>, PageableQueryRequest {
}
