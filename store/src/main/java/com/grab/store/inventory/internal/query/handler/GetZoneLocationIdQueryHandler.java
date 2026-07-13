package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.repository.ZoneRepository;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.query.GetZoneLocationIdQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetZoneLocationIdQueryHandler implements QueryHandler<GetZoneLocationIdQuery, String> {

    private final ZoneRepository zoneRepository;

    @Override
    @InventoryReadTransactional
    public String handle(GetZoneLocationIdQuery query) {
        return zoneRepository.findById(query.zoneId())
                .map(zone -> zone.getLocationId().getValue())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.ZoneNotFound(query.zoneId().getValue())));
    }

    @Override
    public Class<GetZoneLocationIdQuery> getQueryType() {
        return GetZoneLocationIdQuery.class;
    }
}
