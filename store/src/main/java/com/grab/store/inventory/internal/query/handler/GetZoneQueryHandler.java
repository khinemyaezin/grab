package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.repository.ZoneRepository;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.query.GetZoneQuery;
import com.grab.store.inventory.internal.query.GetZoneResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetZoneQueryHandler implements QueryHandler<GetZoneQuery, GetZoneResult> {

    private final ZoneRepository zoneRepository;

    @Override
    @InventoryReadTransactional
    public GetZoneResult handle(GetZoneQuery query) {
        Zone zone = zoneRepository.findById(query.zoneId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.ZoneNotFound(query.zoneId().getValue())));

        return new GetZoneResult(
                zone.getId(),
                zone.getLocationId(),
                zone.getCode(),
                zone.getName(),
                zone.getType().name(),
                zone.isActive()
        );
    }

    @Override
    public Class<GetZoneQuery> getQueryType() {
        return GetZoneQuery.class;
    }
}
