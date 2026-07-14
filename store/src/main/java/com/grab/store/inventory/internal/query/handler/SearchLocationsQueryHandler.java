package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.SearchLocationsQuery;
import com.grab.store.inventory.internal.query.SearchLocationsResult;
import com.inventory.infrastructure.repository.jpa.LocationQueryRepository;
import com.inventory.infrastructure.specification.jpa.LocationSearchCriteria;
import com.inventory.infrastructure.view.LocationView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchLocationsQueryHandler implements QueryHandler<SearchLocationsQuery, Page<SearchLocationsResult>> {

    private final LocationQueryRepository locationQueryRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryReadTransactional
    public Page<SearchLocationsResult> handle(SearchLocationsQuery query) {
        LocationSearchCriteria criteria = new LocationSearchCriteria(
                query.merchantId().getValue(),
                query.query(),
                query.type(),
                query.active()
        );
        return locationQueryRepository.search(criteria, query.pageable())
                .map(this::toResult);
    }

    @Override
    public Class<SearchLocationsQuery> getQueryType() {
        return SearchLocationsQuery.class;
    }

    private SearchLocationsResult toResult(LocationView view) {
        return new SearchLocationsResult(
                idGenerator.convertIdFrom(view.uuid()),
                view.code(),
                view.name(),
                view.type().name(),
                view.active(),
                new SearchLocationsResult.Address(
                        view.street(),
                        view.street2(),
                        view.city(),
                        view.state(),
                        view.postalCode(),
                        view.country()
                )
        );
    }
}
