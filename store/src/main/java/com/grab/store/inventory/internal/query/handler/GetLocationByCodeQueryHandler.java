package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.GetLocationByCodeQuery;
import com.grab.store.inventory.internal.query.GetLocationResult;
import com.grab.store.inventory.internal.support.LocationResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetLocationByCodeQueryHandler implements QueryHandler<GetLocationByCodeQuery, GetLocationResult> {

    private final LocationRepository locationRepository;

    @Override
    @InventoryReadTransactional
    public GetLocationResult handle(GetLocationByCodeQuery query) {
        Location location = locationRepository.findByCode(query.code())
                .orElseThrow(() -> new IllegalArgumentException("Location not found for code: " + query.code()));
        return LocationResultMapper.toQueryResult(location);
    }

    @Override
    public Class<GetLocationByCodeQuery> getQueryType() {
        return GetLocationByCodeQuery.class;
    }
}
