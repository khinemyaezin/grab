package com.saleschannel.application.model.read;

import com.grab.framework.cqrs.query.PageableQueryRequest;
import com.grab.framework.cqrs.query.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record ListSalesChannelsQuery(
        String merchantId,
        Pageable pageable
) implements Query<Page<SalesChannelResult>>, PageableQueryRequest {
}
