package com.inventory.application.service;

import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.application.port.inbound.GetLocationUseCase;
import com.inventory.application.port.outbound.LocationQueryPort;
import com.inventory.application.model.read.GetLocationQuery;
import com.inventory.application.model.read.GetLocationResult;
import com.inventory.application.util.InventoryQueryResultMapper;
import com.grab.framework.id.IdGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetLocationService implements GetLocationUseCase {

    private final LocationQueryPort locationQueryPort;
    private final IdGenerator idGenerator;

    public GetLocationResult execute(GetLocationQuery query) {
        return locationQueryPort.findById(query.locationId().getValue())
                .map(view -> InventoryQueryResultMapper.toLocationResult(view, idGenerator))
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(query.locationId().getValue())));
    }

    public Class<GetLocationQuery> getQueryType() {
        return GetLocationQuery.class;
    }
}
