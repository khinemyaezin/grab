package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.GetLocationResult;
import com.grab.store.inventory.internal.query.ListLocationsQuery;
import com.grab.store.inventory.internal.query.ListLocationsResult;
import com.grab.store.inventory.internal.support.LocationResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListLocationsQueryHandler implements QueryHandler<ListLocationsQuery, ListLocationsResult> {

    private final LocationRepository locationRepository;

    @Override
    @InventoryReadTransactional
    public ListLocationsResult handle(ListLocationsQuery query) {
        List<Location> locations = fetchByFilter(query);
        List<GetLocationResult> items = locations.stream()
                .map(LocationResultMapper::toQueryResult)
                .toList();
        return new ListLocationsResult(items);
    }

    @Override
    public Class<ListLocationsQuery> getQueryType() {
        return ListLocationsQuery.class;
    }

    private List<Location> fetchByFilter(ListLocationsQuery query) {
        if (query.type() != null) {
            List<Location> byType = locationRepository.findByType(query.type());
            if (query.active() == null) {
                return byType;
            }
            return byType.stream().filter(location -> location.isActive() == query.active()).toList();
        }

        if (query.active() == null) {
            return locationRepository.findAll();
        }

        if (query.active()) {
            return locationRepository.findAllActive();
        }

        return locationRepository.findAll().stream()
                .filter(location -> !location.isActive())
                .toList();
    }
}
