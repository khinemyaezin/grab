package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.SearchZonesQuery;
import com.grab.store.inventory.internal.query.SearchZonesResult;
import com.inventory.infrastructure.repository.jpa.ZoneQueryRepository;
import com.inventory.infrastructure.specification.jpa.ZoneSearchCriteria;
import com.inventory.infrastructure.view.ZoneView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchZonesQueryHandler implements QueryHandler<SearchZonesQuery, Page<SearchZonesResult>> {

    private final ZoneQueryRepository zoneQueryRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryReadTransactional
    public Page<SearchZonesResult> handle(SearchZonesQuery query) {
        Id locationId = query.locationId();
        ZoneSearchCriteria criteria = new ZoneSearchCriteria(
                query.merchantId().getValue(),
                locationId != null ? locationId.getValue() : null,
                query.query(),
                query.type(),
                query.active()
        );
        return zoneQueryRepository.search(criteria, query.pageable())
                .map(this::toResult);
    }

    @Override
    public Class<SearchZonesQuery> getQueryType() {
        return SearchZonesQuery.class;
    }

    private SearchZonesResult toResult(ZoneView view) {
        return new SearchZonesResult(
                idGenerator.convertIdFrom(view.uuid()),
                idGenerator.convertIdFrom(view.locationId()),
                view.code(),
                view.name(),
                view.type().name(),
                view.active()
        );
    }
}
