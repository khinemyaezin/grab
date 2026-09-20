package com.inventory.application.service;

import com.inventory.application.port.inbound.ListZonesByLocationUseCase;

import com.grab.framework.id.IdGenerator;
import com.inventory.application.model.read.ListZonesByLocationQuery;
import com.inventory.application.model.read.ListZonesResult;
import com.inventory.application.port.outbound.ZoneQueryPort;
import com.inventory.application.model.read.ZoneView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
public class ListZonesByLocationService implements ListZonesByLocationUseCase {

    private final ZoneQueryPort zoneRepository;
    private final IdGenerator idGenerator;

            public Page<ListZonesResult> execute(ListZonesByLocationQuery query) {
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

        public Class<ListZonesByLocationQuery> getQueryType() {
        return ListZonesByLocationQuery.class;
    }
}
