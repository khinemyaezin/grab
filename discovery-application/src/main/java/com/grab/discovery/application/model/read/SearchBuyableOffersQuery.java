package com.grab.discovery.application.model.read;

import com.grab.framework.cqrs.query.PageableQueryRequest;
import com.grab.framework.cqrs.query.Query;
import com.grab.framework.id.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record SearchBuyableOffersQuery(
        Id salesChannelId,
        Pageable pageable
) implements Query<Page<DiscoveryProductView>>, PageableQueryRequest {
}
