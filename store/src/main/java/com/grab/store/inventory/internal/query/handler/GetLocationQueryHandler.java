package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.GetLocationQuery;
import com.grab.store.inventory.internal.query.GetLocationResult;
import com.grab.store.inventory.internal.support.LocationResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetLocationQueryHandler implements QueryHandler<GetLocationQuery, GetLocationResult> {

    private final LocationRepository locationRepository;

    @Override
    @InventoryReadTransactional
    public GetLocationResult handle(GetLocationQuery query) {
        Location location = locationRepository.findById(query.locationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + query.locationId().getValue()));
        return LocationResultMapper.toQueryResult(location);
    }

    @Override
    public Class<GetLocationQuery> getQueryType() {
        return GetLocationQuery.class;
    }
}
