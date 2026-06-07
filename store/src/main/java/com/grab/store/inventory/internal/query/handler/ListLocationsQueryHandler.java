package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.ListLocationsQuery;
import com.grab.store.inventory.internal.query.ListLocationsResult;
import com.inventory.infrastructure.repository.jpa.LocationQueryRepository;
import com.inventory.infrastructure.view.LocationView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListLocationsQueryHandler implements QueryHandler<ListLocationsQuery, Page<ListLocationsResult>> {
    private final LocationQueryRepository locationRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryReadTransactional
    public Page<ListLocationsResult> handle(ListLocationsQuery query) {
        return locationRepository.queryAll(query.sellerId().getValue(), query.pageable())
                .map(this::convertToLocation);
    }

    @Override
    public Class<ListLocationsQuery> getQueryType() {
        return ListLocationsQuery.class;
    }

    private ListLocationsResult convertToLocation(LocationView locationView){
        return new ListLocationsResult(
                idGenerator.convertIdFrom(locationView.uuid()),
                locationView.code(),
                locationView.name(),
                locationView.type().name(),
                locationView.active(),
                new ListLocationsResult.Address(
                        locationView.street(),
                        locationView.street2(),
                        locationView.city(),
                        locationView.state(),
                        locationView.postalCode(),
                        locationView.country()
                )
        );
    }
}
