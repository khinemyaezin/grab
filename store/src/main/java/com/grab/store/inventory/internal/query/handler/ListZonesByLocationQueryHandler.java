package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.ListZonesByLocationQuery;
import com.grab.store.inventory.internal.query.ListZonesResult;
import com.inventory.infrastructure.repository.jpa.ZoneQueryRepository;
import com.inventory.infrastructure.view.ZoneView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListZonesByLocationQueryHandler implements QueryHandler<ListZonesByLocationQuery, Page<ListZonesResult>> {

    private final ZoneQueryRepository zoneRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryReadTransactional
    public Page<ListZonesResult> handle(ListZonesByLocationQuery query) {
        Page<ZoneView> zones = zoneRepository.queryByLocationId(query.locationId().getValue(), query.pageable());
        return zones.map(zone-> new ListZonesResult(
                idGenerator.convertIdFrom(zone.uuid()),
                idGenerator.convertIdFrom(zone.locationId()),
                zone.code(),
                zone.name(),
                zone.type().name(),
                zone.active()
        ));
    }

    @Override
    public Class<ListZonesByLocationQuery> getQueryType() {
        return ListZonesByLocationQuery.class;
    }
}
