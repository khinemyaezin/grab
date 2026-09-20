package com.inventory.application.service;

import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.application.port.inbound.GetZoneLocationIdUseCase;
import com.inventory.application.port.outbound.ZoneQueryPort;
import com.inventory.application.model.read.GetZoneLocationIdQuery;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetZoneLocationIdService implements GetZoneLocationIdUseCase {

    private final ZoneQueryPort zoneQueryPort;

    public String execute(GetZoneLocationIdQuery query) {
        return zoneQueryPort.findById(query.zoneId().getValue())
                .map(zone -> zone.locationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.ZoneNotFound(query.zoneId().getValue())));
    }

    public Class<GetZoneLocationIdQuery> getQueryType() {
        return GetZoneLocationIdQuery.class;
    }
}
