package com.inventory.application.service;

import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.application.port.inbound.GetZoneUseCase;
import com.inventory.application.port.outbound.ZoneQueryPort;
import com.inventory.application.model.read.GetZoneQuery;
import com.inventory.application.model.read.GetZoneResult;
import com.inventory.application.util.InventoryQueryResultMapper;
import com.grab.framework.id.IdGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetZoneService implements GetZoneUseCase {

    private final ZoneQueryPort zoneQueryPort;
    private final IdGenerator idGenerator;

    public GetZoneResult execute(GetZoneQuery query) {
        return zoneQueryPort.findById(query.zoneId().getValue())
                .map(view -> InventoryQueryResultMapper.toZoneResult(view, idGenerator))
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.ZoneNotFound(query.zoneId().getValue())));
    }

    public Class<GetZoneQuery> getQueryType() {
        return GetZoneQuery.class;
    }
}
