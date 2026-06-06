package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.query.GetLocationQuery;
import com.grab.store.inventory.internal.query.GetLocationResult;
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
                .orElseThrow(() -> new InventoryServiceException(new InventoryServiceError.LocationNotFound(query.locationId().getValue())));

        return new GetLocationResult(
                location.getId(),
                location.getCode(),
                location.getName(),
                location.getType().name(),
                location.isActive(),
                new GetLocationResult.Address(
                        location.getAddress().line1(),
                        location.getAddress().line2(),
                        location.getAddress().city(),
                        location.getAddress().state(),
                        location.getAddress().postalCode(),
                        location.getAddress().country()
                )
        );
    }

    @Override
    public Class<GetLocationQuery> getQueryType() {
        return GetLocationQuery.class;
    }
}
